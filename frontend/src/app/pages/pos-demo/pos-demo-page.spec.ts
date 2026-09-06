import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { PosDemoPage } from './pos-demo-page';

describe('PosDemoPage', () => {
  it('shows the external APIs and can validate a code', async () => {
    await TestBed.configureTestingModule({
      imports: [PosDemoPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(PosDemoPage);
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('/external/coupons/validate');
    expect(text).toContain('/external/coupons/redeem');
    expect(text).toContain('X-Api-Key: pos-demo-key');

    const component = fixture.componentInstance;
    component.form.setValue({
      couponCode: 'FF1234ABCD2345',
      channel: 'POS',
      locationId: 'STORE-1',
      reference: 'DEMO-TXN-1001',
    });
    component.validate();

    const http = TestBed.inject(HttpTestingController);
    const req = http.expectOne(`${environment.apiBaseUrl}/external/coupons/validate`);
    expect(req.request.headers.get('X-Api-Key')).toBe('pos-demo-key');
    req.flush({
      couponCode: 'FF1234ABCD2345',
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
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('markedUsed=false');
    http.verify();
  });
});
