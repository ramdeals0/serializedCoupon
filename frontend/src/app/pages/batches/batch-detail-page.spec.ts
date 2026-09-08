import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { BatchDetailPage } from './batch-detail-page';

describe('BatchDetailPage', () => {
  it('displays returned sample coupon codes', async () => {
    await TestBed.configureTestingModule({
      imports: [BatchDetailPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'batch-1' } } },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(BatchDetailPage);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/coupon-batches/batch-1`).flush({
      id: 'batch-1',
      couponId: 'coupon-99',
      rmsCouponId: 'RMS-COUPON-1001',
      rmsCouponCode: 'FALL26',
      rmsCouponName: 'Fall 2026 BOGO',
      couponProgramCode: '1234',
      requestedQuantity: 2,
      generatedQuantity: 2,
      startAt: '2026-09-01T00:00:00Z',
      expiresAt: '2026-12-31T23:59:59Z',
      status: 'COMPLETED',
      createdBy: null,
      externalReference: 'FALL',
      createdAt: '2026-09-04T00:00:00Z',
      updatedAt: '2026-09-04T00:00:00Z',
      sampleCouponCodes: ['FF1234ABCD2345', 'FF1234ZX98QW76'],
    });
    http.expectOne(`${environment.apiBaseUrl}/coupon-batches/batch-1/coupons?page=0&size=20`).flush({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
    });
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('FF1234ABCD2345');
    expect(text).toContain('FF1234ZX98QW76');
    http.verify();
  });
});
