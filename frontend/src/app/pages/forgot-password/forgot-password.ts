import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ForgotPasswordForm } from './component/forgot-password-form/forgot-password-form';

@Component({
  selector: 'app-forgot-password',
  imports: [ForgotPasswordForm],
  templateUrl: 'forgot-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPassword {}
