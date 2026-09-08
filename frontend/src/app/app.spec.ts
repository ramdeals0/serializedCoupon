import { provideHttpClient } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.setItem(
      'serializedCoupon.auth',
      JSON.stringify({
        token: 'test-token',
        tokenType: 'Bearer',
        expiresAt: '2099-01-01T00:00:00Z',
        username: 'admin',
        displayName: 'Administrator',
        role: 'ADMIN',
      }),
    );
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideRouter([])],
    }).compileComponents();
  });

  afterEach(() => sessionStorage.clear());

  it('should create the app shell', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render navigation', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Serialized Coupons');
    expect(compiled.querySelector('.brand-logo')?.getAttribute('alt')).toBe('SkillNet');
    expect(compiled.textContent).toContain('Dashboard');
    expect(compiled.textContent).toContain('Create');
    expect(compiled.textContent).toContain('Batch');
    expect(compiled.textContent).toContain('Search');
    expect(compiled.textContent).toContain('POS Demo');
    expect(compiled.textContent).toContain('Help');
    expect(compiled.textContent).toContain('Administrator');
  });

  it('signs out to the landing page', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.componentInstance.logout();
    expect(navigate).toHaveBeenCalledWith('/');
    expect(fixture.componentInstance.auth.isLoggedIn()).toBe(false);
  });
});
