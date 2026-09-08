import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HelpPage } from './help-page';

describe('HelpPage', () => {
  it('explains the operations menu, code format, and POS demo', async () => {
    await TestBed.configureTestingModule({
      imports: [HelpPage],
      providers: [provideZonelessChangeDetection(), provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(HelpPage);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Help');
    expect(text).toContain('Dashboard');
    expect(text).toContain('Create a coupon, then a batch');
    expect(text).toContain('FF[0-9]{4}[A-Z0-9]{8}');
    expect(text).toContain('Open POS Demo');
    expect(text).toContain('Sign in');
  });
});
