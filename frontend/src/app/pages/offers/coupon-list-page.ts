import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Coupon, CouponStatus, PageResponse } from '../../core/models/api.models';
import { CouponApiService } from '../../core/services/coupon-api.service';

@Component({
  selector: 'app-coupon-list-page',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './coupon-list-page.html',
})
export class CouponListPage implements OnInit {
  private readonly api = inject(CouponApiService);
  private readonly fb = inject(FormBuilder);

  readonly statuses: CouponStatus[] = ['ACTIVE', 'CANCELLED'];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal<PageResponse<Coupon> | null>(null);
  readonly currentPage = signal(0);

  readonly filters = this.fb.nonNullable.group({
    rmsCouponId: [''],
    couponProgramCode: [''],
    status: ['' as CouponStatus | ''],
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
          this.error.set('Unable to load coupons.');
          this.loading.set(false);
        },
      });
  }
}
