export type ToastType = 'error' | 'success' | 'info' | 'warning';

export interface ToastOptions {
  /** Auto-dismiss duration in ms. Default 4000. Pass 0 to keep until dismissed. */
  duration?: number;
}

export interface ToastMessage {
  id: number;
  type: ToastType;
  message: string;
  duration: number;
}
