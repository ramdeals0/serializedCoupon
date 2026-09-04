import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GenerationConfig, RmsCouponDefinition } from '../../core/models/api.models';
import { CouponBatchApiService } from '../../core/services/coupon-batch-api.service';
import { DashboardApiService } from '../../core/services/dashboard-api.service';
import { NotificationService } from '../../core/services/notification.service';
import { RmsCouponApiService } from '../../core/services/rms-coupon-api.service';
import {
  couponProgramCodeValidator,
  startBeforeExpiresValidator,
  toDateTimeLocalValue,
  toIsoUtc,
} from '../../core/validation/coupon-validators';

@Component({
  selector: 'app-create-batch-page',
  imports: [ReactiveFormsModule],
  templateUrl: './create-batch-page.html',
})
export class CreateBatchPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly rmsApi = inject(RmsCouponApiService);
  private readonly batchApi = inject(CouponBatchApiService);
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly couponCodePattern = '^FF[0-9]{4}[A-Z0-9]{8}$';
  readonly rmsOptions = signal<RmsCouponDefinition[]>([]);
  readonly selectedRms = signal<RmsCouponDefinition | null>(null);
  readonly config = signal<GenerationConfig | null>(null);
  readonly submitting = signal(false);
  readonly loadingRms = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      rmsCouponId: ['', Validators.required],
      couponProgramCode: ['', [Validators.required, couponProgramCodeValidator]],
      quantity: [10, [Validators.required, Validators.min(1)]],
      startAt: [toDateTimeLocalValue(), Validators.required],
      expiresAt: [toDateTimeLocalValue(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000)), Validators.required],
      externalReference: [''],
    },
    { validators: startBeforeExpiresValidator },
  );

  ngOnInit(): void {
    this.loadingRms.set(true);
    this.rmsApi.search(undefined, undefined, 0, 50).subscribe({
      next: (page) => {
        this.rmsOptions.set(page.content);
        this.loadingRms.set(false);
      },
      error: () => this.loadingRms.set(false),
    });
    this.dashboardApi.generationConfig().subscribe({
      next: (config) => {
        this.config.set(config);
        this.form.controls.quantity.addValidators(Validators.max(config.maxBatchSize));
        this.form.controls.quantity.updateValueAndValidity();
      },
    });
    this.form.controls.rmsCouponId.valueChanges.subscribe((id) => {
      this.selectedRms.set(this.rmsOptions().find((item) => item.rmsCouponId === id) ?? null);
    });
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.submitting.set(true);
    this.batchApi
      .create(
        {
          rmsCouponId: value.rmsCouponId,
          couponProgramCode: value.couponProgramCode,
          quantity: Number(value.quantity),
          startAt: toIsoUtc(value.startAt),
          expiresAt: toIsoUtc(value.expiresAt),
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
