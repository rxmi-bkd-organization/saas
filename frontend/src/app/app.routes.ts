import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { SignIn } from './pages/sign-in/sign-in';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
import { ResetPassword } from './pages/reset-password/reset-password';
import { PasswordUpdated } from './pages/password-updated/password-updated';
import { EmailSent } from './pages/email-sent/email-sent';
import { EnableAccount } from './pages/enable-account/enable-account';
import { SignUp } from './pages/sign-up/sign-up';
import { Profile } from './pages/profile/profile';
import { Admin } from './pages/admin/admin';
import { NotFound } from './pages/not-found/not-found';
import { AccessDenied } from './pages/access-denied/access-denied';
import { guestGuard } from './core/guard/guest.guard';
import { authenticationGuard } from './core/guard/authentication.guard';
import { adminGuard } from './core/guard/role.guard';
import { ROUTE_PATHS } from './core/constant/route.constant';

export const routes: Routes = [
  {
    path: ROUTE_PATHS.home,
    component: Home,
  },
  {
    path: ROUTE_PATHS.home,
    canActivate: [guestGuard],
    children: [
      {
        path: ROUTE_PATHS.signIn,
        component: SignIn,
      },
      {
        path: ROUTE_PATHS.signUp,
        component: SignUp,
      },
      {
        path: ROUTE_PATHS.forgotPassword,
        component: ForgotPassword,
      },
      {
        path: ROUTE_PATHS.resetPassword,
        component: ResetPassword,
      },
      {
        path: ROUTE_PATHS.passwordUpdated,
        component: PasswordUpdated,
      },
      {
        path: ROUTE_PATHS.emailSent,
        component: EmailSent,
      },
      {
        path: ROUTE_PATHS.enableAccount,
        component: EnableAccount,
      },
    ],
  },
  {
    path: ROUTE_PATHS.home,
    canActivate: [authenticationGuard],
    children: [
      {
        path: ROUTE_PATHS.profile,
        component: Profile,
      },
    ],
  },
  {
    path: ROUTE_PATHS.admin,
    canActivate: [authenticationGuard, adminGuard],
    component: Admin,
  },
  {
    path: ROUTE_PATHS.accessDenied,
    component: AccessDenied,
  },
  {
    path: '**',
    component: NotFound,
  },
];
