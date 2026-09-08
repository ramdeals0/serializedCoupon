import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { CreateCouponPage } from './create-coupon-page';

describe('CreateCouponPage', () => {
  async function setup() {
    const navigate = (commands: unknown[]) => Promise.resolve(true);
    const router = { navigate };
    await TestBed.configureTestingModule({
      imports: [CreateCouponPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: Router, useValue: router },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(CreateCouponPage);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    return { fixture, http, component: fixture.componentInstance, router };
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

  it('requires POS and ATG codes when source is Both', async () => {
    const { component, http } = await setup();
    component.form.patchValue({
      title: 'Fall 2026 BOGO',
      couponProgramCode: '1234',
      couponSource: 'BOTH',
      posCode: '',
      atgCode: '',
    });
    expect(component.form.hasError('sourceCodesRequired')).toBe(true);
    http.verify();
  });

  it('creates a coupon then continues to batch generation', async () => {
    const { component, http, router } = await setup();
    const navigateCalls: unknown[][] = [];
    router.navigate = (commands: unknown[]) => {
      navigateCalls.push(commands);
      return Promise.resolve(true);
    };
    component.form.patchValue({
      title: 'Fall 2026 BOGO',
      description: 'Buy one get one',
      usageLimit: 1,
      couponProgramCode: '1234',
      posCode: 'RMS-COUPON-1001',
      atgCode: 'FALL26-ATG',
      couponSource: 'BOTH',
      startAt: '2026-09-10T00:00',
      expiresAt: '2026-12-31T23:59',
    });
    expect(component.form.valid).toBe(true);
    component.submit();
    const req = http.expectOne(`${environment.apiBaseUrl}/coupons`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.title).toBe('Fall 2026 BOGO');
    expect(req.request.body.description).toBe('Buy one get one');
    expect(req.request.body.usageLimit).toBe(1);
    expect(req.request.body.couponProgramCode).toBe('1234');
    expect(req.request.body.posCode).toBe('RMS-COUPON-1001');
    expect(req.request.body.atgCode).toBe('FALL26-ATG');
    expect(req.request.body.couponSource).toBe('BOTH');
    expect(req.request.body.quantity).toBeUndefined();
    expect(req.request.body.startAt).toContain('T');
    expect(req.request.body.expiresAt).toContain('T');
    req.flush({
      id: 'coupon-99',
      title: 'Fall 2026 BOGO',
      description: 'Buy one get one',
      usageLimit: 1,
      posCode: 'RMS-COUPON-1001',
      atgCode: 'FALL26-ATG',
      couponSource: 'BOTH',
      rmsCouponId: 'RMS-COUPON-1001',
      rmsCouponCode: 'RMS-COUPON-1001',
      rmsCouponName: 'Fall 2026 BOGO',
      couponProgramCode: '1234',
      startAt: '2026-09-10T00:00:00.000Z',
      expiresAt: '2026-12-31T23:59:00.000Z',
      status: 'ACTIVE',
      createdBy: 'manager',
      createdAt: '2026-09-06T00:00:00Z',
      updatedAt: '2026-09-06T00:00:00Z',
    });
    expect(navigateCalls).toEqual([['/coupons', 'coupon-99', 'batches', 'new']]);
    http.verify();
  });
});
