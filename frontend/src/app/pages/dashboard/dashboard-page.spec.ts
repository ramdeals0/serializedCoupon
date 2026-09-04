import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { DashboardPage } from './dashboard-page';

describe('DashboardPage', () => {
  it('shows a loading state and then an error state', async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardPage);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading dashboard');

    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${environment.apiBaseUrl}/dashboard`).flush(
      { title: 'Internal server error' },
      { status: 500, statusText: 'Server Error' },
    );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Unable to load dashboard statistics.');
    http.verify();
  });
});
