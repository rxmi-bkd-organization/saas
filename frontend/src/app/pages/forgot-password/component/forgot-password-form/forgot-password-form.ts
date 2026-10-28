import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { email, form, required } from '@angular/forms/signals';

import { AuthenticationService } from '../../../../core/service/authentication.service';
import { ROUTE_URLS } from '../../../../core/constant/route.constant';
import { ErrorMessageService } from '../../../../core/service/error-message.service';
import { ForgotPasswordForm as ForgotPasswordFormValue } from '../../../../core/model/form/forgot-password.form';
import { FormContainer } from '../../../../shared/ui/form-container/form-container';
import { FormField } from '../../../../shared/ui/form-field/form-field';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-forgot-password-form',
  imports: [FormContainer, FormField, RouterLink, TranslocoPipe],
  templateUrl: './forgot-password-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPasswordForm {
  protected readonly ROUTE_URLS = ROUTE_URLS;

  private readonly router = inject(Router);
  private readonly authenticationService = inject(AuthenticationService);
  private readonly errorMessageService = inject(ErrorMessageService);

  private readonly model = signal<ForgotPasswordFormValue>({ email: '' });

  protected readonly forgotPasswordForm = form(
    this.model,
    (schema) => {
      required(schema.email, {
        message: () => this.errorMessageService.required('email'),
      });
      email(schema.email, {
        message: () => this.errorMessageService.invalid('email'),
      });
    },
    {
      submission: {
        action: async () => {
          try {
            await this.authenticationService.sendForgottenPasswordEmail(this.model().email);
            await this.router.navigateByUrl(ROUTE_URLS.emailSent);
            return undefined;
          } catch (error) {
            return {
              kind: 'forgot-password',
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
          return this.errorMessageService.invalid('email');
        case 404:
          return this.errorMessageService.accountNotFound();
        case 429:
          return this.errorMessageService.tooManyAttempts();
        default:
          return this.errorMessageService.serverError();
      }
    }
    return this.errorMessageService.serverError();
  }
}
