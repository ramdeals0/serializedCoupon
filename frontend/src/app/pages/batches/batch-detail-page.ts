import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  CouponBatch,
  PageResponse,
  SerializedCoupon,
  SerializedCouponStatus,
} from '../../core/models/api.models';
import { CouponBatchApiService } from '../../core/services/coupon-batch-api.service';
import { NotificationService } from '../../core/services/notification.service';
import { SerializedCouponApiService } from '../../core/services/serialized-coupon-api.service';

@Component({
  selector: 'app-batch-detail-page',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './batch-detail-page.html',
})
export class BatchDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly batchApi = inject(CouponBatchApiService);
  private readonly couponApi = inject(SerializedCouponApiService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly batch = signal<CouponBatch | null>(null);
  readonly coupons = signal<PageResponse<SerializedCoupon> | null>(null);
  readonly couponPage = signal(0);
  readonly exporting = signal(false);
  readonly statuses: SerializedCouponStatus[] = [
    'ACTIVE',
    'PENDING',
    'REDEEMED',
    'EXPIRED',
    'DEACTIVATED',
    'CANCELLED',
  ];

  readonly couponFilters = this.fb.nonNullable.group({
    query: [''],
    status: ['' as SerializedCouponStatus | ''],
  });

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    const batchId = this.route.snapshot.paramMap.get('batchId');
    if (!batchId) {
      this.error.set('Missing batch id.');
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.batchApi.get(batchId).subscribe({
      next: (batch) => {
        this.batch.set(batch);
        this.loading.set(false);
        this.loadCoupons(0);
      },
      error: () => {
        this.error.set('Unable to load this coupon batch.');
        this.loading.set(false);
      },
    });
  }

  loadCoupons(page = 0): void {
    const batch = this.batch();
    if (!batch) {
      return;
    }
    const filters = this.couponFilters.getRawValue();
    this.batchApi
      .listCoupons(batch.id, {
        query: filters.query || undefined,
        status: filters.status,
        page,
        size: 20,
      })
      .subscribe({
        next: (result) => {
          this.coupons.set(result);
          this.couponPage.set(page);
        },
      });
  }

  exportCsv(): void {
    const batch = this.batch();
    if (!batch) {
      return;
    }
    this.exporting.set(true);
    this.batchApi.exportCsv(batch.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `serialized-coupons-${batch.id}.csv`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: () => this.exporting.set(false),
    });
  }

  async copyCode(code: string): Promise<void> {
    await navigator.clipboard.writeText(code);
    this.notifications.info(`Copied ${code}`);
  }

  deactivate(code: string): void {
    if (!confirm(`Deactivate coupon ${code}? This cannot be undone.`)) {
      return;
    }
    this.couponApi.deactivate(code).subscribe({
      next: () => {
        this.notifications.success(`Deactivated ${code}`);
        this.loadCoupons(this.couponPage());
      },
    });
  }

  canDeactivate(coupon: SerializedCoupon): boolean {
    return coupon.status === 'ACTIVE' || coupon.status === 'PENDING';
  }
}
