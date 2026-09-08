import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ExternalCouponRequest, ExternalCouponResponse } from '../models/api.models';

export function resolveApiBaseUrl(): string {
  const base = environment.apiBaseUrl.replace(/\/$/, '');
  if (/^https?:\/\//i.test(base)) {
    return base;
  }
  const origin = globalThis.location?.origin ?? '';
  return `${origin}${base.startsWith('/') ? base : `/${base}`}`;
}

@Injectable({ providedIn: 'root' })
export class ExternalCouponApiService {
  private readonly http = inject(HttpClient);
  readonly apiBaseUrl = resolveApiBaseUrl();
  readonly apiKey = environment.externalApiKey;
  readonly validateUrl = `${this.apiBaseUrl}/external/coupons/validate`;
  readonly redeemUrl = `${this.apiBaseUrl}/external/coupons/redeem`;

  validate(request: ExternalCouponRequest): Observable<ExternalCouponResponse> {
    return this.post(this.validateUrl, request);
  }

  redeem(request: ExternalCouponRequest): Observable<ExternalCouponResponse> {
    return this.post(this.redeemUrl, request);
  }

  private post(url: string, request: ExternalCouponRequest): Observable<ExternalCouponResponse> {
    return this.http.post<ExternalCouponResponse>(url, request, {
      headers: {
        'X-Api-Key': this.apiKey,
        'X-Skip-Auth': '1',
        'X-Skip-Error-Notification': '1',
      },
    });
  }
}
