import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

export type ConfirmDialogTone = 'danger' | 'primary';

let nextConfirmDialogId = 0;

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.scss',
})
export class ConfirmDialogComponent {
  @Input({ required: true }) title = '';
  @Input({ required: true }) message = '';
  @Input() confirmLabel = 'Xác nhận';
  @Input() cancelLabel = 'Hủy';
  @Input() busyLabel = 'Đang xử lý...';
  @Input() busy = false;
  @Input() error = '';
  @Input() iconUrl: string | null = null;
  @Input() iconName = 'warning';
  @Input() tone: ConfirmDialogTone = 'danger';

  @Output() confirmed = new EventEmitter<void>();
  @Output() dismissed = new EventEmitter<void>();

  readonly titleId = `confirm-dialog-title-${++nextConfirmDialogId}`;
  readonly descriptionId = `confirm-dialog-description-${nextConfirmDialogId}`;

  confirm(): void {
    if (!this.busy) this.confirmed.emit();
  }

  dismiss(): void {
    if (!this.busy) this.dismissed.emit();
  }

  dismissFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.dismiss();
  }

  @HostListener('document:keydown.escape')
  dismissWithEscape(): void {
    this.dismiss();
  }
}
