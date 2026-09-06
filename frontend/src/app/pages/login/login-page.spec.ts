import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginPage } from './login-page';

describe('LoginPage', () => {
  it('shows an error for invalid credentials', async () => {
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginPage);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.setValue({ username: 'admin', password: 'bad' });
    component.submit();

    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/auth/login`).flush(
      { title: 'Unauthorized', detail: 'Invalid username or password' },
      { status: 401, statusText: 'Unauthorized' },
    );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Invalid username or password.');
    http.verify();
  });
});
