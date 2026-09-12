import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';
import { ToastType } from './toast.model';

@Component({
  selector: 'app-toast',
  standalone: true,
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss',
})
export class ToastComponent {
  private readonly toastService = inject(ToastService);

  readonly toasts = this.toastService.toasts;

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }

  iconLabel(type: ToastType): string {
    switch (type) {
      case 'success':
        return 'Thành công';
      case 'info':
        return 'Thông tin';
      case 'warning':
        return 'Cảnh báo';
      case 'error':
      default:
        return 'Lỗi';
    }
  }
}
