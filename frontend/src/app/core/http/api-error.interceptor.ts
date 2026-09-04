import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ProblemDetails } from '../models/api.models';
import { NotificationService } from '../services/notification.service';

export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!req.headers.has('X-Skip-Error-Notification')) {
        notifications.error(extractProblemMessage(error));
      }
      return throwError(() => error);
    }),
  );
};

export function extractProblemMessage(error: HttpErrorResponse): string {
  const problem = error.error as ProblemDetails | null;
  if (problem && typeof problem === 'object') {
    if (problem.detail) {
      return problem.detail;
    }
    if (problem.title) {
      return problem.title;
    }
  }
  if (typeof error.error === 'string' && error.error.trim().length > 0) {
    return error.error;
  }
  return error.message || 'Request failed';
}
