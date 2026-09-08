import { afterEach, describe, expect, it, vi } from 'vitest';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('clears a generated-coupons notice after 45 seconds', () => {
    vi.useFakeTimers();
    const service = new NotificationService();
    service.success('Generated 20 coupons.');
    expect(service.notice()?.message).toBe('Generated 20 coupons.');
    vi.advanceTimersByTime(NotificationService.dismissAfterMs - 1);
    expect(service.notice()).not.toBeNull();
    vi.advanceTimersByTime(1);
    expect(service.notice()).toBeNull();
  });

  it('restarts the dismiss timer when a later notice is shown', () => {
    vi.useFakeTimers();
    const service = new NotificationService();
    service.success('Generated 20 coupons.');
    vi.advanceTimersByTime(20_000);
    service.info('Copied FF1234ABCD2345');
    vi.advanceTimersByTime(20_000);
    expect(service.notice()?.message).toBe('Copied FF1234ABCD2345');
    vi.advanceTimersByTime(25_000);
    expect(service.notice()).toBeNull();
  });
});
