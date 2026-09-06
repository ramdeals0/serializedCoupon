import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly origin = globalThis.location?.origin ?? '';

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const target = returnUrl && this.canOpen(returnUrl) ? returnUrl : this.auth.homePath();
        void this.router.navigateByUrl(target);
      },
      error: () => {
        this.error.set('Invalid username or password.');
        this.submitting.set(false);
      },
    });
  }

  private canOpen(url: string): boolean {
    if (url.startsWith('/coupons')) {
      return this.auth.can('create');
    }
    if (url.startsWith('/coupon-batches')) {
      return this.auth.can('batches');
    }
    if (url.startsWith('/serialized-coupons')) {
      return this.auth.can('search');
    }
    if (url === '/' || url.startsWith('/?')) {
      return this.auth.can('dashboard');
    }
    return true;
  }
}
