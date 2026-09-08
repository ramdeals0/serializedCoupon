import { Injectable, signal } from '@angular/core';

export interface AppNotice {
  kind: 'success' | 'error' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  static readonly dismissAfterMs = 45_000;

  readonly notice = signal<AppNotice | null>(null);
  private dismissTimer: ReturnType<typeof setTimeout> | null = null;

  success(message: string): void {
    this.show({ kind: 'success', message });
  }

  error(message: string): void {
    this.show({ kind: 'error', message });
  }

  info(message: string): void {
    this.show({ kind: 'info', message });
  }

  clear(): void {
    this.clearTimer();
    this.notice.set(null);
  }

  private show(notice: AppNotice): void {
    this.clearTimer();
    this.notice.set(notice);
    this.dismissTimer = setTimeout(() => {
      this.notice.set(null);
      this.dismissTimer = null;
    }, NotificationService.dismissAfterMs);
  }

  private clearTimer(): void {
    if (this.dismissTimer !== null) {
      clearTimeout(this.dismissTimer);
      this.dismissTimer = null;
    }
  }
}
