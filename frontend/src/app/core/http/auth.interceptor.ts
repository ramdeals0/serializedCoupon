import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const skipAuth = req.headers.has('X-Skip-Auth');
  const token = auth.token();
  let outgoing = req;
  if (skipAuth) {
    outgoing = req.clone({ headers: req.headers.delete('X-Skip-Auth') });
  } else if (token) {
    outgoing = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(outgoing).pipe(
    catchError((error: HttpErrorResponse) => {
      const isLogin = req.url.includes('/auth/login');
      if (error.status === 401 && !isLogin && !skipAuth && auth.isLoggedIn()) {
        auth.logout();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
