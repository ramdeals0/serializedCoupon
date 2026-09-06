import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Coupon, GenerationConfig } from '../../core/models/api.models';
import { CouponApiService } from '../../core/services/coupon-api.service';
import { CouponBatchApiService } from '../../core/services/coupon-batch-api.service';
import { DashboardApiService } from '../../core/services/dashboard-api.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-create-batch-page',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './create-batch-page.html',
})
export class CreateBatchPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly couponApi = inject(CouponApiService);
  private readonly batchApi = inject(CouponBatchApiService);
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly couponCodePattern = '^FF[0-9]{4}[A-Z0-9]{8}$';
  readonly coupon = signal<Coupon | null>(null);
  readonly config = signal<GenerationConfig | null>(null);
  readonly submitting = signal(false);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    quantity: [10, [Validators.required, Validators.min(1)]],
    externalReference: [''],
  });

  ngOnInit(): void {
    const couponId = this.route.snapshot.paramMap.get('couponId');
    if (!couponId) {
      this.error.set('Select a coupon before generating a batch.');
      this.loading.set(false);
      return;
    }
    this.dashboardApi.generationConfig().subscribe({
      next: (config) => {
        this.config.set(config);
        this.form.controls.quantity.addValidators(Validators.max(config.maxBatchSize));
        this.form.controls.quantity.updateValueAndValidity();
      },
    });
    this.couponApi.get(couponId).subscribe({
      next: (coupon) => {
        this.coupon.set(coupon);
        this.loading.set(false);
        if (coupon.status !== 'ACTIVE') {
          this.error.set('Only an active coupon can generate a batch.');
        }
      },
      error: () => {
        this.error.set('Unable to load coupon.');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    const coupon = this.coupon();
    if (!coupon || coupon.status !== 'ACTIVE' || this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.submitting.set(true);
    this.batchApi
      .create(
        {
          couponId: coupon.id,
          quantity: Number(value.quantity),
          externalReference: value.externalReference || null,
        },
        crypto.randomUUID(),
      )
      .subscribe({
        next: (batch) => {
          this.submitting.set(false);
          this.notifications.success(`Generated ${batch.generatedQuantity} coupons.`);
          void this.router.navigate(['/coupon-batches', batch.id]);
        },
        error: () => this.submitting.set(false),
      });
  }
}
