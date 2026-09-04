import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CouponValidation,
  PageResponse,
  SerializedCoupon,
  SerializedCouponStatus,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SerializedCouponApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/serialized-coupons`;

  search(filters: {
    query?: string;
    rmsCouponId?: string;
    couponProgramCode?: string;
    batchId?: string;
    status?: SerializedCouponStatus | '';
    validAt?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<SerializedCoupon>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 20));
    if (filters.query) {
      params = params.set('query', filters.query);
    }
    if (filters.rmsCouponId) {
      params = params.set('rmsCouponId', filters.rmsCouponId);
    }
    if (filters.couponProgramCode) {
      params = params.set('couponProgramCode', filters.couponProgramCode);
    }
    if (filters.batchId) {
      params = params.set('batchId', filters.batchId);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.validAt) {
      params = params.set('validAt', filters.validAt);
    }
    return this.http.get<PageResponse<SerializedCoupon>>(this.baseUrl, { params });
  }

  get(couponCode: string): Observable<SerializedCoupon> {
    return this.http.get<SerializedCoupon>(`${this.baseUrl}/${encodeURIComponent(couponCode)}`);
  }

  validate(couponCode: string): Observable<CouponValidation> {
    return this.http.post<CouponValidation>(
      `${this.baseUrl}/${encodeURIComponent(couponCode)}/validate`,
      {},
    );
  }

  deactivate(couponCode: string): Observable<SerializedCoupon> {
    return this.http.post<SerializedCoupon>(
      `${this.baseUrl}/${encodeURIComponent(couponCode)}/deactivate`,
      {},
    );
  }
}
