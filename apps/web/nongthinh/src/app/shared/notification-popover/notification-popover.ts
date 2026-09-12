import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationStore } from '../../core/realtime/notification.store';
import { InAppNotification } from '../../core/realtime/realtime.models';

@Component({
  selector: 'app-notification-popover',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-popover.html',
  styleUrl: './notification-popover.scss',
})
export class NotificationPopover {
  readonly store = inject(NotificationStore);
  readonly open = signal(false);
  private readonly element: ElementRef<HTMLElement> = inject(ElementRef);
  private readonly router = inject(Router);

  toggle(): void {
    this.open.update(value => !value);
    if (this.open()) this.store.refresh();
  }
  @HostListener('document:click', ['$event'])
  outside(event: MouseEvent): void {
    if (!this.element.nativeElement.contains(event.target as Node)) this.open.set(false);
  }
  @HostListener('document:keydown.escape')
  escape(): void {
    if (!this.open()) return;
    this.open.set(false);
    this.element.nativeElement.querySelector<HTMLButtonElement>('.notification-button')?.focus();
  }
  visit(item: InAppNotification): void {
    if (!item.readAt) this.store.markRead(item.id);
    this.open.set(false);
    if (item.type === 'POST_REACTION' || item.type === 'POST_COMMENT'
        || item.type === 'COMMENT_REPLY' || item.type === 'POST_HIDDEN') {
      void this.router.navigate(['/app/community', item.entityId]);
      return;
    }
    void this.router.navigate(['/app/community']);
  }
  label(item: InAppNotification): string {
    if (item.type === 'POST_REACTION') return 'Có người bày tỏ cảm xúc về bài viết của bạn.';
    if (item.type === 'COMMENT_REPLY') return 'Có người trả lời bình luận của bạn.';
    if (item.type === 'POST_COMMENT') return 'Có người bình luận về bài viết của bạn.';
    if (item.type === 'REPORT_RESOLVED') return 'Báo cáo của bạn đã được giải quyết.';
    if (item.type === 'REPORT_REJECTED') return 'Báo cáo của bạn đã được xem xét và bác bỏ.';
    if (item.type === 'POST_HIDDEN') return 'Bài viết bị báo cáo của bạn đã bị ẩn.';
    if (item.type === 'POST_DELETED') return 'Bài viết bị báo cáo của bạn đã bị xóa.';
    return 'Một báo cáo về bài viết của bạn đã được xem xét và bác bỏ.';
  }
}
