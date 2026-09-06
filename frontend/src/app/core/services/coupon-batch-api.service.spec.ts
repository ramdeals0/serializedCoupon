import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CouponBatchApiService } from './coupon-batch-api.service';

describe('CouponBatchApiService', () => {
  it('sends creation requests with an idempotency key', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), CouponBatchApiService],
    });
    const service = TestBed.inject(CouponBatchApiService);
    const http = TestBed.inject(HttpTestingController);
    const payload = {
      couponId: 'coupon-99',
      quantity: 10,
      externalReference: 'FALL-2026-CAMPAIGN',
    };

    service.create(payload, 'idem-123').subscribe();

    const req = http.expectOne(`${environment.apiBaseUrl}/coupon-batches`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Idempotency-Key')).toBe('idem-123');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 'batch-1', sampleCouponCodes: [] });
    http.verify();
  });

  it('applies search filters as query params', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), CouponBatchApiService],
    });
    const service = TestBed.inject(CouponBatchApiService);
    const http = TestBed.inject(HttpTestingController);

    service.search({ rmsCouponId: 'RMS-COUPON-1001', couponProgramCode: '1234', page: 1, size: 10 }).subscribe();

    const req = http.expectOne(
      (request) =>
        request.url === `${environment.apiBaseUrl}/coupon-batches` &&
        request.params.get('rmsCouponId') === 'RMS-COUPON-1001' &&
        request.params.get('couponProgramCode') === '1234' &&
        request.params.get('page') === '1',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
    http.verify();
  });
});
