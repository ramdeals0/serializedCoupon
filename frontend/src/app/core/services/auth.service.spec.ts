import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AuthService],
    });
  });

  afterEach(() => sessionStorage.clear());

  it('stores a session and exposes role permissions', () => {
    const service = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);

    service.login('manager', 'Manager123!').subscribe();
    const req = http.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.body).toEqual({ username: 'manager', password: 'Manager123!' });
    req.flush({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresAt: '2099-01-01T00:00:00Z',
      username: 'manager',
      displayName: 'Manager',
      role: 'MANAGER',
    });

    expect(service.isLoggedIn()).toBe(true);
    expect(service.can('create')).toBe(true);
    expect(service.can('search')).toBe(true);
    expect(service.can('dashboard')).toBe(false);
    expect(service.can('deactivate')).toBe(false);
    expect(service.homePath()).toBe('/coupons/new');
    http.verify();
  });

  it('limits customer service to coupon status lookup', () => {
    const service = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    service.login('csr', 'Csr123!').subscribe();
    http.expectOne(`${environment.apiBaseUrl}/auth/login`).flush({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresAt: '2099-01-01T00:00:00Z',
      username: 'csr',
      displayName: 'Customer Service',
      role: 'CUSTOMER_SERVICE',
    });
    expect(service.can('search')).toBe(true);
    expect(service.can('create')).toBe(false);
    expect(service.homePath()).toBe('/serialized-coupons');
    http.verify();
  });

  it('sends administrators to the operations dashboard', () => {
    const service = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    service.login('admin', 'Admin123!').subscribe();
    http.expectOne(`${environment.apiBaseUrl}/auth/login`).flush({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresAt: '2099-01-01T00:00:00Z',
      username: 'admin',
      displayName: 'Administrator',
      role: 'ADMIN',
    });
    expect(service.homePath()).toBe('/dashboard');
    http.verify();
  });
});
