import { inject } from '@angular/core';
import { AuthenticationService } from '../service/authentication.service';
import { Router } from '@angular/router';
import { ROUTE_URLS } from '../constant/route.constant';

export function guestGuard() {
  const router = inject(Router);
  const authenticationService = inject(AuthenticationService);
  return !authenticationService.isAuthenticated() ? true : router.createUrlTree([ROUTE_URLS.home]);
}
