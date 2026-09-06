import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LandingPage } from './landing-page';

describe('LandingPage', () => {
  it('explains serialized coupons and the operations flow', async () => {
    await TestBed.configureTestingModule({
      imports: [LandingPage],
      providers: [provideZonelessChangeDetection(), provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(LandingPage);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Serialized coupons that stay unique from issue to redemption');
    expect(text).toContain('Create the coupon');
    expect(text).toContain('Generate a batch');
    expect(text).toContain('Validate or redeem');
    expect(text).toContain('Sign in to operations');
    expect(text).toContain('Try the POS demo');
  });
});
