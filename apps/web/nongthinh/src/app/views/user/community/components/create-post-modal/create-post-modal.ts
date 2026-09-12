import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  inject,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CropTypeView } from '../../../../../core/api/agri-catalog-api.service';
import {
  PostMediaType,
  PostTopicView,
  PostTypeView,
} from '../../../../../core/api/post-api.service';
import { UserAvatarComponent } from '../../../../../shared/user-avatar/user-avatar.component';
import { NewCommunityPost, PostCategory } from '../../models/community.models';

type PostOption = 'TOPIC' | 'CROP';

interface SelectedPostMedia {
  file: File;
  mediaType: PostMediaType;
  previewUrl: string;
}

@Component({
  selector: 'app-create-post-modal',
  standalone: true,
  imports: [ReactiveFormsModule, UserAvatarComponent],
  templateUrl: './create-post-modal.html',
  styleUrl: './create-post-modal.scss',
})
export class CreatePostModal implements OnInit, OnChanges, OnDestroy {
  private static readonly IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
  private static readonly VIDEO_TYPES = new Set(['video/mp4', 'video/webm', 'video/quicktime']);
  private static readonly MAX_IMAGE_BYTES = 5 * 1024 * 1024;
  private static readonly MAX_VIDEO_BYTES = 50 * 1024 * 1024;
  private static readonly MAX_MEDIA_COUNT = 10;
  private readonly fb = inject(FormBuilder);

  @Input() userName = '';
  @Input() avatarUrl: string | null = null;
  @Input() initialCategory: PostCategory | null = null;
  @Input() postTypes: PostTypeView[] = [];
  @Input() postTopics: PostTopicView[] = [];
  @Input() cropTypes: CropTypeView[] = [];
  @Input() cropTypesError = '';
  @Input() submitting = false;
  @Input() serverError = '';
  @Output() dismissed = new EventEmitter<void>();
  @Output() postCreated = new EventEmitter<NewCommunityPost>();

  mediaError = '';
  selectedMedia: SelectedPostMedia[] = [];
  activeOption: PostOption | null = null;
  topicSearch = '';
  cropSearch = '';

  readonly form = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(2000)]],
    postTypeId: [''],
    topicId: [''],
    location: ['', Validators.maxLength(120)],
    cropTypeIds: this.fb.nonNullable.control<string[]>([]),
  });

  ngOnInit(): void {
    this.selectDefaultPostType();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['postTypes'] || changes['initialCategory']) this.selectDefaultPostType();
  }

  ngOnDestroy(): void {
    this.revokeMediaPreviews();
  }

  @HostListener('document:keydown.escape')
  closeOnEscape(): void {
    this.dismissed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.dismissed.emit();
    }
  }

  onMediaSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = '';
    if (!files.length) return;

    this.mediaError = '';
    const accepted: Array<{ file: File; mediaType: PostMediaType }> = [];
    for (const file of files) {
      const mediaType = this.mediaTypeOf(file);
      if (!mediaType) {
        this.mediaError = 'Một số tệp không đúng định dạng JPG, PNG, WEBP, MP4, WEBM hoặc MOV.';
        continue;
      }
      const maxBytes =
        mediaType === 'VIDEO' ? CreatePostModal.MAX_VIDEO_BYTES : CreatePostModal.MAX_IMAGE_BYTES;
      if (file.size > maxBytes) {
        this.mediaError =
          mediaType === 'VIDEO'
            ? 'Video không được lớn hơn 50 MB.'
            : 'Mỗi ảnh không được lớn hơn 5 MB.';
        continue;
      }
      accepted.push({ file, mediaType });
    }

    const includesVideo = accepted.some((item) => item.mediaType === 'VIDEO');
    if (
      (includesVideo && (accepted.length > 1 || this.selectedMedia.length > 0)) ||
      (this.selectedMedia.some((item) => item.mediaType === 'VIDEO') && accepted.length > 0)
    ) {
      this.mediaError = 'Video chỉ có thể được chọn riêng; một bài viết hỗ trợ một video.';
      return;
    }

    const existingKeys = new Set(this.selectedMedia.map((item) => this.fileKey(item.file)));
    const availableSlots = CreatePostModal.MAX_MEDIA_COUNT - this.selectedMedia.length;
    const uniqueAccepted = accepted.filter((item) => {
      const key = this.fileKey(item.file);
      if (existingKeys.has(key)) return false;
      existingKeys.add(key);
      return true;
    });
    const additions = uniqueAccepted
      .slice(0, availableSlots)
      .map((item) => ({ ...item, previewUrl: URL.createObjectURL(item.file) }));
    this.selectedMedia = [...this.selectedMedia, ...additions];

    if (uniqueAccepted.length > additions.length) {
      this.mediaError = `Mỗi bài viết được chọn tối đa ${CreatePostModal.MAX_MEDIA_COUNT} ảnh.`;
    }
  }

  removeMedia(index: number): void {
    const item = this.selectedMedia[index];
    if (!item) return;
    URL.revokeObjectURL(item.previewUrl);
    this.selectedMedia = this.selectedMedia.filter((_, currentIndex) => currentIndex !== index);
    this.mediaError = '';
  }

  openMediaPicker(fileInput: HTMLInputElement): void {
    this.activeOption = null;
    fileInput.click();
  }

  toggleOption(option: PostOption): void {
    this.activeOption = this.activeOption === option ? null : option;
  }

  updateTopicSearch(event: Event): void {
    this.topicSearch = (event.target as HTMLInputElement).value;
  }

  updateCropSearch(event: Event): void {
    this.cropSearch = (event.target as HTMLInputElement).value;
  }

  filteredPostTopics(): PostTopicView[] {
    const search = this.normalizeSearchValue(this.topicSearch);
    if (!search) return this.postTopics;
    return this.postTopics.filter((topic) =>
      this.normalizeSearchValue(topic.name).includes(search),
    );
  }

  filteredCropTypes(): CropTypeView[] {
    const search = this.normalizeSearchValue(this.cropSearch);
    if (!search) return this.cropTypes;
    return this.cropTypes.filter((cropType) =>
      this.normalizeSearchValue(cropType.name).includes(search),
    );
  }

  toggleCropType(cropTypeId: string, checked: boolean): void {
    const selected = this.form.controls.cropTypeIds.value;
    const next = checked
      ? [...new Set([...selected, cropTypeId])]
      : selected.filter((id) => id !== cropTypeId);
    this.form.controls.cropTypeIds.setValue(next);
    this.form.controls.cropTypeIds.markAsDirty();
  }

  submit(): void {
    const content = this.form.controls.content.value.trim();

    if (!content || this.form.invalid || this.submitting) {
      this.form.markAllAsTouched();
      return;
    }

    this.postCreated.emit({
      content,
      postTypeId: this.form.controls.postTypeId.value || null,
      topicId: this.form.controls.topicId.value || null,
      location: this.form.controls.location.value.trim(),
      cropTypeIds: [...this.form.controls.cropTypeIds.value],
      mediaFiles: this.selectedMedia.map(({ file, mediaType }) => ({ file, mediaType })),
    });
  }

  private mediaTypeOf(file: File): PostMediaType | null {
    const contentType = file.type.toLowerCase();
    if (CreatePostModal.IMAGE_TYPES.has(contentType)) return 'IMAGE';
    if (CreatePostModal.VIDEO_TYPES.has(contentType)) return 'VIDEO';
    return null;
  }

  private fileKey(file: File): string {
    return `${file.name}:${file.size}:${file.lastModified}`;
  }

  private revokeMediaPreviews(): void {
    this.selectedMedia.forEach((item) => URL.revokeObjectURL(item.previewUrl));
  }

  private normalizeSearchValue(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLocaleLowerCase('vi-VN')
      .replace(/đ/g, 'd')
      .trim();
  }

  private selectDefaultPostType(): void {
    const current = this.form.controls.postTypeId.value;
    if (current && this.postTypes.some((item) => item.id === current)) return;
    if (!this.postTypes.length || !this.initialCategory) {
      this.form.controls.postTypeId.setValue('');
      return;
    }
    const preferredCodes =
      this.initialCategory === 'MARKET'
        ? ['MARKET', 'PRICE']
        : this.initialCategory === 'MODEL'
          ? ['MODEL', 'EXPERIENCE']
          : this.initialCategory
            ? [this.initialCategory]
            : [];
    const preferred = preferredCodes
      .map((code) => this.postTypes.find((item) => item.code === code))
      .find((item) => item != null);
    this.form.controls.postTypeId.setValue(preferred?.id ?? '');
  }
}
