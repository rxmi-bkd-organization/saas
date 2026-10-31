import { ChangeDetectionStrategy, Component } from '@angular/core';

import { SignInForm } from './component/sign-in-form/sign-in-form';

@Component({
  selector: 'app-sign-in',
  imports: [SignInForm],
  templateUrl: './sign-in.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignIn {}
