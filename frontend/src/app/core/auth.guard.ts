import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "./auth.service";
export const authGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  return auth.session()?.user.role === route.data["role"]
    ? true
    : inject(Router).createUrlTree(["/login"]);
};
