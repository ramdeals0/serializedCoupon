import { Injectable, signal } from '@angular/core';

export interface AppNotice {
  kind: 'success' | 'error' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly notice = signal<AppNotice | null>(null);

  success(message: string): void {
    this.notice.set({ kind: 'success', message });
  }

  error(message: string): void {
    this.notice.set({ kind: 'error', message });
  }

  info(message: string): void {
    this.notice.set({ kind: 'info', message });
  }

  clear(): void {
    this.notice.set(null);
  }
}
