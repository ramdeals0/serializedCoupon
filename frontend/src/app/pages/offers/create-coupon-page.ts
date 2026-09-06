import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CouponSource } from '../../core/models/api.models';
import { CouponApiService } from '../../core/services/coupon-api.service';
import { NotificationService } from '../../core/services/notification.service';
import {
  couponProgramCodeValidator,
  sourceCodesValidator,
  startBeforeExpiresValidator,
  toDateTimeLocalValue,
  toIsoUtc,
} from '../../core/validation/coupon-validators';

@Component({
  selector: 'app-create-coupon-page',
  imports: [ReactiveFormsModule],
  templateUrl: './create-coupon-page.html',
})
export class CreateCouponPage {
  private readonly fb = inject(FormBuilder);
  private readonly couponApi = inject(CouponApiService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly sources: { value: CouponSource; label: string }[] = [
    { value: 'POS', label: 'POS' },
    { value: 'ECOMM', label: 'Ecomm' },
    { value: 'BOTH', label: 'Both' },
  ];
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', Validators.maxLength(2000)],
      usageLimit: [1, [Validators.required, Validators.min(1), Validators.max(10000)]],
      couponProgramCode: ['', [Validators.required, couponProgramCodeValidator]],
      posCode: ['', Validators.maxLength(64)],
      atgCode: ['', Validators.maxLength(64)],
      couponSource: ['BOTH' as CouponSource, Validators.required],
      startAt: [toDateTimeLocalValue(), Validators.required],
      expiresAt: [toDateTimeLocalValue(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000)), Validators.required],
    },
    { validators: [startBeforeExpiresValidator, sourceCodesValidator] },
  );

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.submitting.set(true);
    this.couponApi
      .create({
        title: value.title.trim(),
        description: value.description.trim() || null,
        usageLimit: Number(value.usageLimit),
        couponProgramCode: value.couponProgramCode,
        posCode: value.posCode.trim() || null,
        atgCode: value.atgCode.trim() || null,
        couponSource: value.couponSource,
        startAt: toIsoUtc(value.startAt),
        expiresAt: toIsoUtc(value.expiresAt),
      })
      .subscribe({
        next: (coupon) => {
          this.submitting.set(false);
          this.notifications.success('Coupon created. Next, generate a serialized batch.');
          void this.router.navigate(['/coupons', coupon.id, 'batches', 'new']);
        },
        error: () => this.submitting.set(false),
      });
  }
}
