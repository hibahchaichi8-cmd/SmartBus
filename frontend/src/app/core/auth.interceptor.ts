import { inject } from "@angular/core";
import { HttpInterceptorFn } from "@angular/common/http";
import { catchError, throwError } from "rxjs";

import { AuthService } from "./auth.service";

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const own = request.url.startsWith("/api/v1/");

  if (own && token) {
    request = request.clone({
      setHeaders: {
        Authorization: "Bearer " + token,
      },
    });
  }

  return next(request).pipe(
    catchError((error) => {
      if (
        own &&
        token &&
        error.status === 401 &&
        auth.token() === token
      ) {
        auth.logout();
      }

      return throwError(() => error);
    }),
  );
};