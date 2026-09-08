import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Coupon } from '../../core/models/api.models';
import { CouponApiService } from '../../core/services/coupon-api.service';

@Component({
  selector: 'app-coupon-offer-detail-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './coupon-detail-page.html',
})
export class CouponOfferDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(CouponApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly coupon = signal<Coupon | null>(null);

  ngOnInit(): void {
    const couponId = this.route.snapshot.paramMap.get('couponId');
    if (!couponId) {
      this.error.set('Coupon id is missing.');
      this.loading.set(false);
      return;
    }
    this.api.get(couponId).subscribe({
      next: (coupon) => {
        this.coupon.set(coupon);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load coupon.');
        this.loading.set(false);
      },
    });
  }
}
