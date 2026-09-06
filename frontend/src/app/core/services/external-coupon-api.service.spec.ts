import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ExternalCouponApiService } from './external-coupon-api.service';

describe('ExternalCouponApiService', () => {
  it('posts validate and redeem with the demo API key', () => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        ExternalCouponApiService,
      ],
    });
    const api = TestBed.inject(ExternalCouponApiService);
    const http = TestBed.inject(HttpTestingController);
    const body = { couponCode: 'FF1234ABCD2345', channel: 'POS' as const };

    api.validate(body).subscribe();
    const validate = http.expectOne(`${environment.apiBaseUrl}/external/coupons/validate`);
    expect(validate.request.method).toBe('POST');
    expect(validate.request.headers.get('X-Api-Key')).toBe('pos-demo-key');
    validate.flush({
      couponCode: body.couponCode,
      accepted: true,
      markedUsed: false,
      validationReason: 'VALID',
      status: 'ACTIVE',
      channel: 'POS',
      timesUsed: 0,
      usageLimit: 1,
      redeemedAt: null,
      checkedAt: '2026-09-06T12:00:00Z',
    });

    api.redeem({ ...body, locationId: 'STORE-1' }).subscribe();
    const redeem = http.expectOne(`${environment.apiBaseUrl}/external/coupons/redeem`);
    expect(redeem.request.method).toBe('POST');
    expect(redeem.request.headers.get('X-Api-Key')).toBe('pos-demo-key');
    redeem.flush({
      couponCode: body.couponCode,
      accepted: true,
      markedUsed: true,
      validationReason: 'VALID',
      status: 'REDEEMED',
      channel: 'POS',
      timesUsed: 1,
      usageLimit: 1,
      redeemedAt: '2026-09-06T12:00:00Z',
      checkedAt: '2026-09-06T12:00:00Z',
    });
    http.verify();
  });
});
