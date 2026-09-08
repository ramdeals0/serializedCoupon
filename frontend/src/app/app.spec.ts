import { provideHttpClient } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
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
    expect(compiled.textContent).toContain('Coupons');
    expect(compiled.textContent).toContain('Create');
    expect(compiled.textContent).toContain('Administrator');
  });
});
