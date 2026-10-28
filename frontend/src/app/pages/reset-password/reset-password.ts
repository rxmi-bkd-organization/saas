import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';

import { ResetPasswordForm } from './component/reset-password-form/reset-password-form';

@Component({
  selector: 'app-reset-password',
  imports: [ResetPasswordForm],
  templateUrl: './reset-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResetPassword implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly token = signal('');

  async ngOnInit(): Promise<void> {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.token.set(token);
      await this.router.navigate([], { replaceUrl: true, queryParams: {} });
    }
  }
}
