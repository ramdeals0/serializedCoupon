import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse, RmsCouponDefinition } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class RmsCouponApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/rms-coupons`;

  search(query?: string, active?: boolean, page = 0, size = 50): Observable<PageResponse<RmsCouponDefinition>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (query) {
      params = params.set('query', query);
    }
    if (active !== undefined) {
      params = params.set('active', String(active));
    }
    return this.http.get<PageResponse<RmsCouponDefinition>>(this.baseUrl, { params });
  }

  get(rmsCouponId: string): Observable<RmsCouponDefinition> {
    return this.http.get<RmsCouponDefinition>(`${this.baseUrl}/${encodeURIComponent(rmsCouponId)}`);
  }
}
