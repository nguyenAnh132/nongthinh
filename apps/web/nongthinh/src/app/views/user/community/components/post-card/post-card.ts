import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  POST_REACTION_TYPES,
  POST_REPORT_REASONS,
  PostReactionType,
  PostReportReason,
} from '../../../../../core/api/post-api.service';
import { UserAvatarComponent } from '../../../../../shared/user-avatar/user-avatar.component';
import { ConfirmDialogComponent } from '../../../../../shared/confirm-dialog/confirm-dialog';
import { DEFAULT_POST_CATEGORY_META, POST_CATEGORY_META } from '../../data/community.constants';
import { CommunityComment, CommunityPost } from '../../models/community.models';

interface ReactionOption {
  type: PostReactionType;
  icon: string;
  label: string;
}

export interface PostCardCommentCreateEvent {
  content: string;
  parentCommentId: string | null;
}

export interface PostCardCommentUpdateEvent {
  commentId: string;
  content: string;
}

export interface PostCardReportEvent {
  reason: PostReportReason;
  reasonDetail: string | null;
}

interface ReportReasonOption {
  value: PostReportReason;
  label: string;
  description: string;
}

const REACTION_META: Record<PostReactionType, Omit<ReactionOption, 'type'>> = {
  LIKE: { icon: '👍', label: 'Thích' },
  LOVE: { icon: '❤️', label: 'Yêu thích' },
  HAHA: { icon: '😄', label: 'Haha' },
  WOW: { icon: '😮', label: 'Wow' },
  SAD: { icon: '😢', label: 'Buồn' },
  ANGRY: { icon: '😠', label: 'Phẫn nộ' },
};

const REACTION_LONG_PRESS_MS = 450;
const SYNTHETIC_CLICK_GUARD_MS = 750;

const REPORT_REASON_META: Record<PostReportReason, Omit<ReportReasonOption, 'value'>> = {
  SPAM: {
    label: 'Spam hoặc quảng cáo',
    description: 'Nội dung lặp lại, gây nhiễu hoặc quảng cáo không phù hợp.',
  },
  HARASSMENT: {
    label: 'Quấy rối',
    description: 'Công kích, đe dọa hoặc nhắm mục tiêu vào một cá nhân.',
  },
  HATE_SPEECH: {
    label: 'Ngôn từ thù ghét',
    description: 'Kỳ thị hoặc kích động thù ghét đối với một nhóm người.',
  },
  VIOLENCE: { label: 'Bạo lực', description: 'Mô tả, cổ súy hoặc đe dọa hành vi bạo lực.' },
  SEXUAL_CONTENT: {
    label: 'Nội dung tình dục',
    description: 'Nội dung nhạy cảm hoặc tình dục không phù hợp.',
  },
  MISINFORMATION: {
    label: 'Thông tin sai lệch',
    description: 'Thông tin có thể gây hiểu nhầm hoặc gây hại.',
  },
  COPYRIGHT: {
    label: 'Vi phạm bản quyền',
    description: 'Sử dụng nội dung không có quyền hoặc không ghi nguồn.',
  },
  OTHER: { label: 'Lý do khác', description: 'Vấn đề khác chưa có trong danh sách.' },
};

@Component({
  selector: 'app-community-post-card',
  standalone: true,
  imports: [FormsModule, UserAvatarComponent, ConfirmDialogComponent],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss',
})
export class PostCard implements OnChanges, OnDestroy {
  private readonly elementRef = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly imageLightbox = viewChild<ElementRef<HTMLElement>>('imageLightbox');
  private reactionPressTimer: ReturnType<typeof setTimeout> | null = null;
  private reactionLongPressTriggered = false;
  private ignoreReactionClicksUntil = 0;

  @Input({ required: true }) post!: CommunityPost;
  @Input() currentUserId = '';
  @Input() currentUserName = '';
  @Input() currentUserAvatarUrl: string | null = null;
  @Input() interactionsEnabled = false;
  @Input() detailMode = false;
  @Output() detailRequested = new EventEmitter<void>();
  @Output() reactionUsersRequested = new EventEmitter<void>();
  @Output() reactionChanged = new EventEmitter<PostReactionType | null>();
  @Output() commentsRequested = new EventEmitter<boolean>();
  @Output() commentCreated = new EventEmitter<PostCardCommentCreateEvent>();
  @Output() commentUpdated = new EventEmitter<PostCardCommentUpdateEvent>();
  @Output() commentDeleted = new EventEmitter<string>();
  @Output() bookmarkToggled = new EventEmitter<void>();
  @Output() shareRequested = new EventEmitter<void>();
  @Output() reportRequested = new EventEmitter<PostCardReportEvent>();
  @Output() deleteRequested = new EventEmitter<void>();

  readonly reactionOptions: ReactionOption[] = POST_REACTION_TYPES.map((type) => ({
    type,
    ...REACTION_META[type],
  }));
  readonly reportReasonOptions: ReportReasonOption[] = POST_REPORT_REASONS.map((value) => ({
    value,
    ...REPORT_REASON_META[value],
  }));

  commentText = '';
  replyText = '';
  editText = '';
  commentsExpanded = false;
  readonly reactionPickerOpen = signal(false);
  readonly postMenuOpen = signal(false);
  readonly reportDialogOpen = signal(false);
  readonly deleteDialogOpen = signal(false);
  replyingTo: string | null = null;
  editingCommentId: string | null = null;
  reportReason: PostReportReason | '' = '';
  reportDetail = '';
  activeImageIndex: number | null = null;
  imageZoom = 1;

  ngOnChanges(changes: SimpleChanges): void {
    if (this.detailMode) this.commentsExpanded = true;
    const postChange = changes['post'];
    if (!postChange || postChange.firstChange) return;
    const previous = postChange.previousValue as CommunityPost;
    const current = postChange.currentValue as CommunityPost;
    if (previous.commentCreateVersion !== current.commentCreateVersion) {
      this.commentText = '';
      this.replyText = '';
      this.replyingTo = null;
    }
    if (previous.commentEditVersion !== current.commentEditVersion) {
      this.editText = '';
      this.editingCommentId = null;
    }
    if (!previous.reported && current.reported) this.resetReportDialog();
    if (this.activeImageIndex !== null && this.activeImageIndex >= current.imageUrls.length) {
      this.closeImage();
    }
  }

  ngOnDestroy(): void {
    this.cancelReactionPress();
  }

  categoryMeta(category: string | null) {
    return (category ? POST_CATEGORY_META[category] : undefined) ?? DEFAULT_POST_CATEGORY_META;
  }

  categoryLabel(): string {
    return this.post.categoryLabel?.trim() || this.categoryMeta(this.post.category).shortLabel;
  }

  visibleImageUrls(): string[] {
    return this.post.imageUrls.slice(0, 4);
  }

  remainingImageCount(): number {
    return Math.max(0, this.post.imageUrls.length - 4);
  }

  activeImageUrl(): string | null {
    return this.activeImageIndex === null
      ? null
      : (this.post.imageUrls[this.activeImageIndex] ?? null);
  }

  imageZoomPercent(): number {
    return Math.round(this.imageZoom * 100);
  }

  openImage(index: number): void {
    if (!this.post.imageUrls[index]) return;
    this.activeImageIndex = index;
    this.imageZoom = 1;
    requestAnimationFrame(() => this.imageLightbox()?.nativeElement.focus());
  }

  closeImage(): void {
    this.activeImageIndex = null;
    this.imageZoom = 1;
  }

  showPreviousImage(): void {
    if (this.activeImageIndex === null || this.post.imageUrls.length < 2) return;
    this.activeImageIndex =
      (this.activeImageIndex - 1 + this.post.imageUrls.length) % this.post.imageUrls.length;
    this.imageZoom = 1;
  }

  showNextImage(): void {
    if (this.activeImageIndex === null || this.post.imageUrls.length < 2) return;
    this.activeImageIndex = (this.activeImageIndex + 1) % this.post.imageUrls.length;
    this.imageZoom = 1;
  }

  zoomImageIn(): void {
    this.setImageZoom(this.imageZoom + 0.25);
  }

  zoomImageOut(): void {
    this.setImageZoom(this.imageZoom - 0.25);
  }

  resetImageZoom(): void {
    this.imageZoom = 1;
  }

  onImageWheel(event: WheelEvent): void {
    event.preventDefault();
    this.setImageZoom(this.imageZoom + (event.deltaY < 0 ? 0.25 : -0.25));
  }

  activeReactions(): ReactionOption[] {
    return this.reactionOptions.filter((reaction) => this.post.reactionCounts[reaction.type] > 0);
  }

  reactionLabel(type: PostReactionType | null): string {
    return type ? REACTION_META[type].label : 'Thích';
  }

  reactionIcon(type: PostReactionType | null): string {
    return type ? REACTION_META[type].icon : '♡';
  }

  onReactionPointerDown(event: PointerEvent): void {
    if (event.button !== 0 || !this.interactionsEnabled || this.post.reactionPending) {
      return;
    }
    this.cancelReactionPress();
    this.reactionLongPressTriggered = false;
    this.reactionPressTimer = setTimeout(() => {
      this.reactionPressTimer = null;
      this.reactionLongPressTriggered = true;
      this.reactionPickerOpen.set(true);
    }, REACTION_LONG_PRESS_MS);
  }

  onReactionPointerEnd(): void {
    this.cancelReactionPress();
    if (!this.reactionLongPressTriggered) return;
    this.reactionLongPressTriggered = false;
    this.ignoreReactionClicksUntil = Date.now() + SYNTHETIC_CLICK_GUARD_MS;
  }

  onReactionClick(event: MouseEvent): void {
    event.preventDefault();
    if (!this.interactionsEnabled || this.post.reactionPending) return;
    if (Date.now() < this.ignoreReactionClicksUntil) return;
    this.reactionPickerOpen.set(false);
    this.reactionChanged.emit(this.post.currentReaction ? null : 'LIKE');
  }

  selectReaction(type: PostReactionType): void {
    if (!this.interactionsEnabled || this.post.reactionPending) return;
    this.reactionPickerOpen.set(false);
    this.reactionChanged.emit(this.post.currentReaction === type ? null : type);
  }

  @HostListener('document:pointerdown', ['$event'])
  closeMenusOnOutsidePress(event: PointerEvent): void {
    if (event.target instanceof Node) {
      if (this.reactionPickerOpen()) {
        const reactionMenu = this.elementRef.nativeElement.querySelector('.reaction-menu');
        if (reactionMenu && !reactionMenu.contains(event.target)) {
          this.reactionPickerOpen.set(false);
        }
      }

      if (this.postMenuOpen()) {
        const postMenu = this.elementRef.nativeElement.querySelector('.post-menu');
        if (postMenu && !postMenu.contains(event.target)) this.postMenuOpen.set(false);
      }
    }
  }

  togglePostMenu(): void {
    this.reactionPickerOpen.set(false);
    this.postMenuOpen.update((open) => !open);
  }

  toggleComments(): void {
    if (!this.interactionsEnabled) return;
    if (this.detailMode) {
      if (!this.post.commentsLoaded && !this.post.commentsLoading) this.commentsRequested.emit(false);
      this.elementRef.nativeElement.querySelector('.comments')?.scrollIntoView({ behavior: 'smooth' });
      return;
    }
    this.commentsExpanded = !this.commentsExpanded;
    if (this.commentsExpanded && !this.post.commentsLoaded && !this.post.commentsLoading) {
      this.commentsRequested.emit(false);
    }
  }

  retryComments(): void {
    if (!this.post.commentsLoading) this.commentsRequested.emit(this.post.commentsLoaded);
  }

  openDetails(event: MouseEvent): void {
    if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
    event.preventDefault();
    if (!this.detailMode) this.detailRequested.emit();
  }

  onCardClick(event: MouseEvent): void {
    if (this.detailMode || !(event.target instanceof Element)) return;
    if (event.target.closest('a, button, input, textarea, video, [role="dialog"], .comments')) return;
    if (window.getSelection()?.toString()) return;
    this.openDetails(event);
  }

  loadMoreComments(): void {
    if (this.post.commentsHasNext && !this.post.commentsLoading) {
      this.commentsRequested.emit(true);
    }
  }

  submitComment(): void {
    const content = this.commentText.trim();
    if (!content || this.post.commentMutationPending) return;
    this.commentCreated.emit({ content, parentCommentId: null });
  }

  startReply(comment: CommunityComment): void {
    if (this.post.commentMutationPending) return;
    this.editingCommentId = null;
    this.editText = '';
    if (this.replyingTo !== comment.id) this.replyText = '';
    this.replyingTo = comment.id;
  }

  cancelReply(): void {
    this.replyingTo = null;
    this.replyText = '';
  }

  submitReply(parentCommentId: string): void {
    const content = this.replyText.trim();
    if (!content || this.post.commentMutationPending) return;
    this.commentCreated.emit({ content, parentCommentId });
  }

  startEdit(comment: CommunityComment): void {
    if (!this.isOwn(comment) || this.post.commentMutationPending) return;
    this.replyingTo = null;
    this.replyText = '';
    this.editingCommentId = comment.id;
    this.editText = comment.content;
  }

  cancelEdit(): void {
    this.editingCommentId = null;
    this.editText = '';
  }

  submitEdit(commentId: string): void {
    const content = this.editText.trim();
    if (!content || this.post.commentMutationPending) return;
    this.commentUpdated.emit({ commentId, content });
  }

  requestDelete(comment: CommunityComment): void {
    if (!this.isOwn(comment) || this.post.commentMutationPending) return;
    if (typeof window !== 'undefined' && !window.confirm('Xóa bình luận này?')) return;
    this.commentDeleted.emit(comment.id);
  }

  isOwn(comment: CommunityComment): boolean {
    return Boolean(this.currentUserId) && comment.author.id === this.currentUserId;
  }

  canReportPost(): boolean {
    return (
      this.interactionsEnabled &&
      Boolean(this.currentUserId) &&
      this.post.author.id !== this.currentUserId &&
      !this.post.reported
    );
  }

  canDeletePost(): boolean {
    return Boolean(this.currentUserId) && this.post.author.id === this.currentUserId;
  }

  openDeleteDialog(): void {
    if (!this.canDeletePost() || this.post.deletePending) return;
    this.postMenuOpen.set(false);
    this.deleteDialogOpen.set(true);
  }

  closeDeleteDialog(): void {
    if (this.post.deletePending) return;
    this.deleteDialogOpen.set(false);
  }

  confirmDelete(): void {
    if (!this.canDeletePost() || this.post.deletePending) return;
    this.deleteRequested.emit();
  }

  openReportDialog(): void {
    if (!this.canReportPost() || this.post.reportPending) return;
    this.postMenuOpen.set(false);
    this.reportDialogOpen.set(true);
  }

  closeReportDialog(): void {
    if (this.post.reportPending) return;
    this.resetReportDialog();
  }

  closeReportDialogFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeReportDialog();
  }

  submitReport(): void {
    if (!this.reportReason || !this.canReportPost() || this.post.reportPending) return;
    const reasonDetail = this.reportDetail.trim();
    this.reportRequested.emit({
      reason: this.reportReason,
      reasonDetail: reasonDetail || null,
    });
  }

  @HostListener('document:keydown.escape')
  closeDialogsWithEscape(): void {
    if (this.activeImageIndex !== null) {
      this.closeImage();
      return;
    }
    if (this.deleteDialogOpen()) {
      this.closeDeleteDialog();
      return;
    }
    if (this.reportDialogOpen()) {
      this.closeReportDialog();
      return;
    }
    this.postMenuOpen.set(false);
  }

  @HostListener('document:keydown.arrowleft', ['$event'])
  showPreviousImageWithKeyboard(event: Event): void {
    if (this.activeImageIndex === null) return;
    event.preventDefault();
    this.showPreviousImage();
  }

  @HostListener('document:keydown.arrowright', ['$event'])
  showNextImageWithKeyboard(event: Event): void {
    if (this.activeImageIndex === null) return;
    event.preventDefault();
    this.showNextImage();
  }

  private cancelReactionPress(): void {
    if (this.reactionPressTimer === null) return;
    clearTimeout(this.reactionPressTimer);
    this.reactionPressTimer = null;
  }

  private resetReportDialog(): void {
    this.reportDialogOpen.set(false);
    this.reportReason = '';
    this.reportDetail = '';
  }

  private setImageZoom(value: number): void {
    this.imageZoom = Math.min(4, Math.max(1, Math.round(value * 100) / 100));
  }
}
