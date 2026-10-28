import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../service/authentication.service';
import { ROUTE_URLS } from '../constant/route.constant';

export function adminGuard() {
  const router = inject(Router);
  const authenticationService = inject(AuthenticationService);
  return authenticationService.isAdmin() ? true : router.createUrlTree([ROUTE_URLS.accessDenied]);
}
