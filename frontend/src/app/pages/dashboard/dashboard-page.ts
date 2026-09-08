import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Coupon, DashboardSummary } from '../../core/models/api.models';
import { CouponApiService } from '../../core/services/coupon-api.service';
import { DashboardApiService } from '../../core/services/dashboard-api.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage implements OnInit {
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly couponApi = inject(CouponApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly coupons = signal<Coupon[]>([]);

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
        this.loadCoupons();
      },
      error: () => {
        this.error.set('Unable to load dashboard statistics.');
        this.loading.set(false);
      },
    });
  }

  private loadCoupons(): void {
    this.couponApi.search({ page: 0, size: 10 }).subscribe({
      next: (page) => this.coupons.set(page.content),
      error: () => this.coupons.set([]),
    });
  }
}
