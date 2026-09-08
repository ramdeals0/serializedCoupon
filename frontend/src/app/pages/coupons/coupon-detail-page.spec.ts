import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { CouponDetailPage } from './coupon-detail-page';

describe('CouponDetailPage', () => {
  it('shows usage counts and does not offer validate now', async () => {
    await TestBed.configureTestingModule({
      imports: [CouponDetailPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ couponCode: 'FF4545BZY6FNQA' }) } },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(CouponDetailPage);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/serialized-coupons/FF4545BZY6FNQA`).flush({
      id: 'coupon-1',
      couponCode: 'FF4545BZY6FNQA',
      batchId: 'batch-1',
      rmsCouponId: 'C1234',
      rmsCouponCode: 'C1234',
      couponProgramCode: '4545',
      startAt: '2026-09-01T00:00:00Z',
      expiresAt: '2026-12-31T23:59:59Z',
      status: 'ACTIVE',
      redeemedAt: null,
      deactivatedAt: null,
      timesUsed: 1,
      usageLimit: 2,
      externalReference: null,
      createdAt: '2026-09-06T00:00:00Z',
      updatedAt: '2026-09-06T00:00:00Z',
    });
    http.expectOne(`${environment.apiBaseUrl}/serialized-coupons/FF4545BZY6FNQA/validate`).flush({
      couponCode: 'FF4545BZY6FNQA',
      valid: true,
      validationReason: 'VALID',
      status: 'ACTIVE',
      startAt: '2026-09-01T00:00:00Z',
      expiresAt: '2026-12-31T23:59:59Z',
      rmsCouponId: 'C1234',
      checkedAt: '2026-09-06T12:00:00Z',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).not.toContain('Validate now');
    expect(text).toContain('Times used');
    expect(text).toContain('1');
    expect(text).toContain('Remaining uses');
    expect(text).toContain('POS code');
    expect(text).toContain('Coupon program code');
    expect(text).not.toContain('RMS coupon');
    expect(fixture.componentInstance.remainingUses(fixture.componentInstance.coupon()!)).toBe(1);
    http.verify();
  });
});
