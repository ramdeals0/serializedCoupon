import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthSession, Permission, UserRole } from '../models/api.models';

const STORAGE_KEY = 'serializedCoupon.auth';

const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  ADMIN: ['dashboard', 'batches', 'create', 'search', 'deactivate'],
  MANAGER: ['batches', 'create', 'search'],
  CUSTOMER_SERVICE: ['search'],
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = signal<AuthSession | null>(readStoredSession());

  readonly user = this.session.asReadonly();
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly displayName = computed(() => this.session()?.displayName ?? '');
  readonly role = computed(() => this.session()?.role ?? null);

  token(): string | null {
    return this.session()?.token ?? null;
  }

  can(permission: Permission): boolean {
    const role = this.session()?.role;
    return role ? ROLE_PERMISSIONS[role].includes(permission) : false;
  }

  homePath(): string {
    const role = this.session()?.role;
    if (role === 'MANAGER') {
      return '/coupons/new';
    }
    if (role === 'CUSTOMER_SERVICE') {
      return '/serialized-coupons';
    }
    return '/dashboard';
  }

  login(username: string, password: string) {
    return this.http
      .post<AuthSession>(`${environment.apiBaseUrl}/auth/login`, { username, password }, {
        headers: { 'X-Skip-Error-Notification': '1' },
      })
      .pipe(tap((session) => this.persist(session)));
  }

  logout(): void {
    this.session.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  private persist(session: AuthSession): void {
    this.session.set(session);
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  }
}

function readStoredSession(): AuthSession | null {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    const parsed = JSON.parse(raw) as AuthSession;
    if (!parsed.token || !parsed.role || !parsed.expiresAt) {
      return null;
    }
    if (Date.parse(parsed.expiresAt) <= Date.now()) {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
    return parsed;
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}
