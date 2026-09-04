import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CouponBatch,
  CouponBatchStatus,
  CreateCouponBatchRequest,
  PageResponse,
  SerializedCoupon,
  SerializedCouponStatus,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class CouponBatchApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/coupon-batches`;

  create(request: CreateCouponBatchRequest, idempotencyKey: string): Observable<CouponBatch> {
    const headers = new HttpHeaders({ 'Idempotency-Key': idempotencyKey });
    return this.http.post<CouponBatch>(this.baseUrl, request, { headers });
  }

  search(filters: {
    rmsCouponId?: string;
    couponProgramCode?: string;
    status?: CouponBatchStatus | '';
    createdFrom?: string;
    createdTo?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<CouponBatch>> {
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
    if (filters.createdFrom) {
      params = params.set('createdFrom', filters.createdFrom);
    }
    if (filters.createdTo) {
      params = params.set('createdTo', filters.createdTo);
    }
    return this.http.get<PageResponse<CouponBatch>>(this.baseUrl, { params });
  }

  get(batchId: string): Observable<CouponBatch> {
    return this.http.get<CouponBatch>(`${this.baseUrl}/${batchId}`);
  }

  listCoupons(
    batchId: string,
    filters: {
      status?: SerializedCouponStatus | '';
      query?: string;
      activeAt?: string;
      page?: number;
      size?: number;
    },
  ): Observable<PageResponse<SerializedCoupon>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 20));
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.query) {
      params = params.set('query', filters.query);
    }
    if (filters.activeAt) {
      params = params.set('activeAt', filters.activeAt);
    }
    return this.http.get<PageResponse<SerializedCoupon>>(`${this.baseUrl}/${batchId}/coupons`, { params });
  }

  exportCsv(batchId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${batchId}/export`, { responseType: 'blob' });
  }
}
