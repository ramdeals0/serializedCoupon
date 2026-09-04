import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardSummary, GenerationConfig } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private readonly http = inject(HttpClient);

  summary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${environment.apiBaseUrl}/dashboard`);
  }

  generationConfig(): Observable<GenerationConfig> {
    return this.http.get<GenerationConfig>(`${environment.apiBaseUrl}/meta/generation-config`);
  }
}
