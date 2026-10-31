import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { form, maxLength, minLength, required } from '@angular/forms/signals';

import { AuthenticationService } from '../../../../core/service/authentication.service';
import { PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from '../../../../core/constant/password.constant';
import { ROUTE_URLS } from '../../../../core/constant/route.constant';
import { ErrorMessageService } from '../../../../core/service/error-message.service';
import { ResetPasswordForm as ResetPasswordFormValue } from '../../../../core/model/form/reset-password.form';
import { FormContainer } from '../../../../shared/ui/form-container/form-container';
import { FormField } from '../../../../shared/ui/form-field/form-field';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-reset-password-form',
  imports: [FormContainer, FormField, TranslocoPipe],
  templateUrl: './reset-password-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResetPasswordForm {
  readonly token = input.required<string>();

  private readonly router = inject(Router);
  private readonly authenticationService = inject(AuthenticationService);
  private readonly errorMessageService = inject(ErrorMessageService);

  private readonly model = signal<ResetPasswordFormValue>({ newPassword: '' });

  protected readonly resetPasswordForm = form(
    this.model,
    (schema) => {
      required(schema.newPassword, {
        message: () => this.errorMessageService.required('password'),
      });
      minLength(schema.newPassword, PASSWORD_MIN_LENGTH, {
        message: () => this.errorMessageService.minLength('password', PASSWORD_MIN_LENGTH),
      });
      maxLength(schema.newPassword, PASSWORD_MAX_LENGTH, {
        message: () => this.errorMessageService.maxLength('password', PASSWORD_MAX_LENGTH),
      });
    },
    {
      submission: {
        action: async () => {
          try {
            await this.authenticationService.resetPassword(this.token(), this.model().newPassword);
            await this.router.navigateByUrl(ROUTE_URLS.passwordUpdated);
            return undefined;
          } catch (error) {
            return {
              kind: 'reset-password',
              message: this.mapErrorToMessage(error),
            };
          }
        },
      },
    },
  );

  private mapErrorToMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 400:
          return this.errorMessageService.invalidResetPasswordRequest();
        case 401:
          return this.errorMessageService.invalidResetToken();
        case 429:
          return this.errorMessageService.tooManyAttempts();
        default:
          return this.errorMessageService.serverError();
      }
    }
    return this.errorMessageService.serverError();
  }
}
