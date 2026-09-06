import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { CreateBatchPage } from './create-batch-page';

describe('CreateBatchPage', () => {
  async function setup() {
    await TestBed.configureTestingModule({
      imports: [CreateBatchPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ couponId: 'coupon-99' }) } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(CreateBatchPage);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/meta/generation-config`).flush({
      minBatchSize: 1,
      maxBatchSize: 10000,
      prefix: 'FF',
      programCodeLength: 4,
      suffixLength: 8,
      totalLength: 14,
      couponCodeRegex: '^FF[0-9]{4}[A-Z0-9]{8}$',
      programCodeRegex: '^[0-9]{4}$',
    });
    http.expectOne(`${environment.apiBaseUrl}/coupons/coupon-99`).flush({
      id: 'coupon-99',
      title: 'Fall 2026 BOGO',
      description: 'Buy one get one',
      usageLimit: 1,
      posCode: 'RMS-COUPON-1001',
      atgCode: 'FALL26-ATG',
      couponSource: 'BOTH',
      rmsCouponId: 'RMS-COUPON-1001',
      rmsCouponCode: 'FALL26',
      rmsCouponName: 'Fall 2026 BOGO',
      couponProgramCode: '1234',
      startAt: '2026-09-10T00:00:00.000Z',
      expiresAt: '2026-12-31T23:59:00.000Z',
      status: 'ACTIVE',
      createdBy: 'manager',
      createdAt: '2026-09-06T00:00:00Z',
      updatedAt: '2026-09-06T00:00:00Z',
    });
    fixture.detectChanges();
    return { fixture, http, component: fixture.componentInstance };
  }

  it('loads the coupon before generation', async () => {
    const { fixture, http } = await setup();
    expect(fixture.nativeElement.textContent).toContain('Fall 2026 BOGO');
    expect(fixture.nativeElement.textContent).toContain('1234');
    expect(fixture.nativeElement.textContent).toContain('Step 2 of 2');
    http.verify();
  });

  it('sends coupon id and quantity only', async () => {
    const { component, http } = await setup();
    component.form.patchValue({
      quantity: 25,
      externalReference: 'FALL-2026-BATCH',
    });
    expect(component.form.valid).toBe(true);
    component.submit();
    const req = http.expectOne(`${environment.apiBaseUrl}/coupon-batches`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Idempotency-Key')).toBeTruthy();
    expect(req.request.body).toEqual({
      couponId: 'coupon-99',
      quantity: 25,
      externalReference: 'FALL-2026-BATCH',
    });
    req.flush({ id: 'batch-99', generatedQuantity: 25, sampleCouponCodes: ['FF1234ABCD2345'] });
    http.verify();
  });
});
