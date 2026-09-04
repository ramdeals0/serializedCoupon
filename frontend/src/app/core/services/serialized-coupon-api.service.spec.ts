import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { SerializedCouponApiService } from './serialized-coupon-api.service';

describe('SerializedCouponApiService', () => {
  it('sends coupon search filters', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), SerializedCouponApiService],
    });
    const service = TestBed.inject(SerializedCouponApiService);
    const http = TestBed.inject(HttpTestingController);

    service.search({ query: 'FF1234', status: 'ACTIVE', rmsCouponId: 'RMS-COUPON-1001' }).subscribe();

    const req = http.expectOne(
      (request) =>
        request.url === `${environment.apiBaseUrl}/serialized-coupons` &&
        request.params.get('query') === 'FF1234' &&
        request.params.get('status') === 'ACTIVE' &&
        request.params.get('rmsCouponId') === 'RMS-COUPON-1001',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true });
    http.verify();
  });
});
