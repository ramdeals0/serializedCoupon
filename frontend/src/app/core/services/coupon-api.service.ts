import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Coupon, CouponStatus, CreateCouponRequest, PageResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class CouponApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/coupons`;

  create(request: CreateCouponRequest): Observable<Coupon> {
    return this.http.post<Coupon>(this.baseUrl, request);
  }

  search(filters: {
    rmsCouponId?: string;
    couponProgramCode?: string;
    status?: CouponStatus | '';
    page?: number;
    size?: number;
  }): Observable<PageResponse<Coupon>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 20));
    if (filters.rmsCouponId) {
      params = params.set('rmsCouponId', filters.rmsCouponId);
    }
    if (filters.couponProgramCode) {
      params = params.set('couponProgramCode', filters.couponProgramCode);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    return this.http.get<PageResponse<Coupon>>(this.baseUrl, { params });
  }

  get(couponId: string): Observable<Coupon> {
    return this.http.get<Coupon>(`${this.baseUrl}/${couponId}`);
  }
}
