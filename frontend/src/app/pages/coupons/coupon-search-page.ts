import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageResponse, SerializedCoupon, SerializedCouponStatus } from '../../core/models/api.models';
import { SerializedCouponApiService } from '../../core/services/serialized-coupon-api.service';
import { toIsoUtc } from '../../core/validation/coupon-validators';

@Component({
  selector: 'app-coupon-search-page',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './coupon-search-page.html',
})
export class CouponSearchPage implements OnInit {
  private readonly api = inject(SerializedCouponApiService);
  private readonly fb = inject(FormBuilder);

  readonly statuses: SerializedCouponStatus[] = [
    'ACTIVE',
    'PENDING',
    'REDEEMED',
    'EXPIRED',
    'DEACTIVATED',
    'CANCELLED',
  ];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal<PageResponse<SerializedCoupon> | null>(null);
  readonly currentPage = signal(0);

  readonly filters = this.fb.nonNullable.group({
    query: [''],
    rmsCouponId: [''],
    couponProgramCode: [''],
    batchId: [''],
    status: ['' as SerializedCouponStatus | ''],
    validAt: [''],
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
        query: value.query || undefined,
        rmsCouponId: value.rmsCouponId || undefined,
        couponProgramCode: value.couponProgramCode || undefined,
        batchId: value.batchId || undefined,
        status: value.status,
        validAt: value.validAt ? toIsoUtc(value.validAt) : undefined,
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
          this.error.set('Unable to search serialized coupons.');
          this.loading.set(false);
        },
      });
  }
}
