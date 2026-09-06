import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CouponValidation, SerializedCoupon } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { SerializedCouponApiService } from '../../core/services/serialized-coupon-api.service';

@Component({
  selector: 'app-coupon-detail-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './coupon-detail-page.html',
})
export class CouponDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(SerializedCouponApiService);
  private readonly notifications = inject(NotificationService);
  readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly coupon = signal<SerializedCoupon | null>(null);
  readonly validation = signal<CouponValidation | null>(null);
  readonly validating = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const couponCode = this.route.snapshot.paramMap.get('couponCode');
    if (!couponCode) {
      this.error.set('Missing coupon code.');
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.api.get(couponCode).subscribe({
      next: (coupon) => {
        this.coupon.set(coupon);
        this.loading.set(false);
        this.validate();
      },
      error: () => {
        this.error.set('Unable to load this serialized coupon.');
        this.loading.set(false);
      },
    });
  }

  validate(): void {
    const coupon = this.coupon();
    if (!coupon) {
      return;
    }
    this.validating.set(true);
    this.api.validate(coupon.couponCode).subscribe({
      next: (result) => {
        this.validation.set(result);
        this.validating.set(false);
      },
      error: () => this.validating.set(false),
    });
  }

  deactivate(): void {
    const coupon = this.coupon();
    if (!coupon || !this.canDeactivate(coupon)) {
      return;
    }
    if (!confirm(`Deactivate coupon ${coupon.couponCode}? This cannot be undone.`)) {
      return;
    }
    this.api.deactivate(coupon.couponCode).subscribe({
      next: (updated) => {
        this.coupon.set(updated);
        this.notifications.success(`Deactivated ${updated.couponCode}`);
        this.validate();
      },
    });
  }

  canDeactivate(coupon: SerializedCoupon): boolean {
    return this.auth.can('deactivate') && (coupon.status === 'ACTIVE' || coupon.status === 'PENDING');
  }
}
