import { ChangeDetectionStrategy, Component } from '@angular/core';

import { SignUpForm } from './component/sign-up-form/sign-up-form';

@Component({
  selector: 'app-sign-up',
  imports: [SignUpForm],
  templateUrl: './sign-up.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignUp {}
