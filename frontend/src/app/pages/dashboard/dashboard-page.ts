import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardSummary } from '../../core/models/api.models';
import { DashboardApiService } from '../../core/services/dashboard-api.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage implements OnInit {
  private readonly dashboardApi = inject(DashboardApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.dashboardApi.summary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load dashboard statistics.');
        this.loading.set(false);
      },
    });
  }
}
