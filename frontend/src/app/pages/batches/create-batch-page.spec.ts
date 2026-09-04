import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
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
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(CreateBatchPage);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/rms-coupons?page=0&size=50`).flush({
      content: [
        {
          id: '1',
          rmsCouponId: 'RMS-COUPON-1001',
          rmsCouponCode: 'FALL26',
          name: 'Fall 2026 BOGO',
          description: 'desc',
          active: true,
          createdAt: null,
          updatedAt: null,
        },
      ],
      page: 0,
      size: 50,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });
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
    fixture.detectChanges();
    return { fixture, http, component: fixture.componentInstance };
  }

  it('rejects an invalid four-digit program code', async () => {
    const { fixture, component, http } = await setup();
    component.form.controls.couponProgramCode.setValue('12ab');
    component.form.controls.couponProgramCode.markAsTouched();
    fixture.detectChanges();
    expect(component.form.controls.couponProgramCode.invalid).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Enter exactly four digits');
    http.verify();
  });

  it('rejects expiration prior to start', async () => {
    const { component, http } = await setup();
    component.form.patchValue({
      startAt: '2026-12-31T10:00',
      expiresAt: '2026-01-01T10:00',
    });
    expect(component.form.hasError('startBeforeExpires')).toBe(true);
    http.verify();
  });

  it('sends the expected creation request', async () => {
    const { component, http } = await setup();
    component.form.patchValue({
      rmsCouponId: 'RMS-COUPON-1001',
      couponProgramCode: '1234',
      quantity: 25,
      startAt: '2026-09-10T00:00',
      expiresAt: '2026-12-31T23:59',
      externalReference: 'FALL-2026-CAMPAIGN',
    });
    expect(component.form.valid).toBe(true);
    component.submit();
    const req = http.expectOne(`${environment.apiBaseUrl}/coupon-batches`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Idempotency-Key')).toBeTruthy();
    expect(req.request.body.rmsCouponId).toBe('RMS-COUPON-1001');
    expect(req.request.body.couponProgramCode).toBe('1234');
    expect(req.request.body.quantity).toBe(25);
    expect(req.request.body.startAt).toContain('T');
    expect(req.request.body.expiresAt).toContain('T');
    req.flush({ id: 'batch-99', generatedQuantity: 25, sampleCouponCodes: ['FF1234ABCD2345'] });
    http.verify();
  });
});
