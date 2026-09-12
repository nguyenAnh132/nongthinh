import { Injectable, signal } from '@angular/core';
import { ToastMessage, ToastOptions, ToastType } from './toast.model';

const DEFAULT_DURATION_MS = 4000;
const MAX_VISIBLE = 3;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<ToastMessage[]>([]);
  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  readonly toasts = this._toasts.asReadonly();

  error(message: string, options?: ToastOptions): void {
    this.show('error', message, options);
  }

  success(message: string, options?: ToastOptions): void {
    this.show('success', message, options);
  }

  info(message: string, options?: ToastOptions): void {
    this.show('info', message, options);
  }

  warning(message: string, options?: ToastOptions): void {
    this.show('warning', message, options);
  }

  dismiss(id: number): void {
    this.clearTimer(id);
    this._toasts.update((list) => list.filter((t) => t.id !== id));
  }

  clear(): void {
    for (const id of this.timers.keys()) {
      this.clearTimer(id);
    }
    this._toasts.set([]);
  }

  private show(type: ToastType, message: string, options?: ToastOptions): void {
    const trimmed = message?.trim();
    if (!trimmed) {
      return;
    }

    const duration =
      options?.duration === undefined ? DEFAULT_DURATION_MS : Math.max(0, options.duration);

    const toast: ToastMessage = {
      id: this.nextId++,
      type,
      message: trimmed,
      duration,
    };

    this._toasts.update((list) => {
      const next = [...list, toast];
      if (next.length <= MAX_VISIBLE) {
        return next;
      }
      const removed = next.slice(0, next.length - MAX_VISIBLE);
      for (const item of removed) {
        this.clearTimer(item.id);
      }
      return next.slice(-MAX_VISIBLE);
    });

    if (duration > 0) {
      const timer = setTimeout(() => this.dismiss(toast.id), duration);
      this.timers.set(toast.id, timer);
    }
  }

  private clearTimer(id: number): void {
    const timer = this.timers.get(id);
    if (timer != null) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
  }
}
