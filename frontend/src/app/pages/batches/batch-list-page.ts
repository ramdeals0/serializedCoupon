import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CouponBatch, CouponBatchStatus, PageResponse } from '../../core/models/api.models';
import { CouponBatchApiService } from '../../core/services/coupon-batch-api.service';

@Component({
  selector: 'app-batch-list-page',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './batch-list-page.html',
})
export class BatchListPage implements OnInit {
  private readonly api = inject(CouponBatchApiService);
  private readonly fb = inject(FormBuilder);

  readonly statuses: CouponBatchStatus[] = [
    'PENDING',
    'PROCESSING',
    'COMPLETED',
    'PARTIALLY_COMPLETED',
    'FAILED',
    'CANCELLED',
  ];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal<PageResponse<CouponBatch> | null>(null);
  readonly currentPage = signal(0);

  readonly filters = this.fb.nonNullable.group({
    rmsCouponId: [''],
    couponProgramCode: [''],
    status: ['' as CouponBatchStatus | ''],
  });

  ngOnInit(): void {
    this.load(0);
  }

  load(pageIndex = 0): void {
    this.loading.set(true);
    this.error.set(null);
    const value = this.filters.getRawValue();
    this.api
      .search({
        rmsCouponId: value.rmsCouponId || undefined,
        couponProgramCode: value.couponProgramCode || undefined,
        status: value.status,
        page: pageIndex,
        size: 20,
      })
      .subscribe({
        next: (result) => {
          this.page.set(result);
          this.currentPage.set(pageIndex);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Unable to load coupon batches.');
          this.loading.set(false);
        },
      });
  }
}
