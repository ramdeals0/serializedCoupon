import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExternalCouponRequest, ExternalCouponResponse, RedeemChannel } from '../../core/models/api.models';
import { ExternalCouponApiService } from '../../core/services/external-coupon-api.service';

@Component({
  selector: 'app-pos-demo-page',
  imports: [JsonPipe, ReactiveFormsModule],
  templateUrl: './pos-demo-page.html',
  styleUrl: './pos-demo-page.css',
})
export class PosDemoPage {
  private readonly api = inject(ExternalCouponApiService);
  private readonly fb = inject(FormBuilder);

  readonly demoPageUrl = `${globalThis.location?.origin ?? ''}/pos-demo`;
  readonly validateUrl = this.api.validateUrl;
  readonly redeemUrl = this.api.redeemUrl;
  readonly apiKey = this.api.apiKey;
  readonly channels: RedeemChannel[] = ['POS', 'ECOMM'];

  readonly submitting = signal(false);
  readonly lastAction = signal<'validate' | 'redeem' | null>(null);
  readonly lastRequest = signal<ExternalCouponRequest | null>(null);
  readonly lastResponse = signal<ExternalCouponResponse | null>(null);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    couponCode: ['', [Validators.required, Validators.pattern(/^FF[0-9]{4}[A-Z0-9]{8}$/)]],
    channel: ['POS' as RedeemChannel, Validators.required],
    locationId: ['STORE-1'],
    reference: ['DEMO-TXN-1001'],
  });

  validate(): void {
    this.call('validate');
  }

  redeem(): void {
    this.call('redeem');
  }

  curl(kind: 'validate' | 'redeem'): string {
    const url = kind === 'validate' ? this.validateUrl : this.redeemUrl;
    const body = this.sampleBody();
    return [
      `curl -s -X POST ${url} \\`,
      `  -H "Content-Type: application/json" \\`,
      `  -H "X-Api-Key: ${this.apiKey}" \\`,
      `  -d '${JSON.stringify(body)}'`,
    ].join('\n');
  }

  private call(kind: 'validate' | 'redeem'): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.sampleBody();
    this.submitting.set(true);
    this.error.set(null);
    this.lastAction.set(kind);
    this.lastRequest.set(request);
    this.lastResponse.set(null);
    const request$ = kind === 'validate' ? this.api.validate(request) : this.api.redeem(request);
    request$.subscribe({
      next: (response) => {
        this.lastResponse.set(response);
        this.submitting.set(false);
      },
      error: (err: { error?: { detail?: string; title?: string }; message?: string }) => {
        this.error.set(err.error?.detail || err.error?.title || err.message || 'Request failed');
        this.submitting.set(false);
      },
    });
  }

  private sampleBody(): ExternalCouponRequest {
    const value = this.form.getRawValue();
    return {
      couponCode: value.couponCode.trim().toUpperCase(),
      channel: value.channel,
      locationId: value.locationId.trim() || null,
      reference: value.reference.trim() || null,
    };
  }
}
