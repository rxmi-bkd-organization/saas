import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { email, form, required } from '@angular/forms/signals';

import { AuthenticationService } from '../../../../core/service/authentication.service';
import { ROUTE_URLS } from '../../../../core/constant/route.constant';
import { ErrorMessageService } from '../../../../core/service/error-message.service';
import { SignInForm as SignInFormValue } from '../../../../core/model/form/sign-in.form';
import { FormContainer } from '../../../../shared/ui/form-container/form-container';
import { FormField } from '../../../../shared/ui/form-field/form-field';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-sign-in-form',
  imports: [FormContainer, FormField, RouterLink, TranslocoPipe],
  templateUrl: './sign-in-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignInForm {
  protected readonly ROUTE_URLS = ROUTE_URLS;

  private readonly router = inject(Router);
  private readonly authenticationService = inject(AuthenticationService);
  private readonly errorMessageService = inject(ErrorMessageService);

  private readonly model = signal<SignInFormValue>({ email: '', password: '' });

  protected readonly signInForm = form(
    this.model,
    (schema) => {
      required(schema.email, {
        message: () => this.errorMessageService.required('email'),
      });
      email(schema.email, {
        message: () => this.errorMessageService.invalid('email'),
      });
      required(schema.password, {
        message: () => this.errorMessageService.required('password'),
      });
    },
    {
      submission: {
        action: async () => {
          const { email, password } = this.model();
          try {
            await this.authenticationService.login(email, password);
            await this.router.navigateByUrl(ROUTE_URLS.home);
            return undefined;
          } catch (error) {
            return {
              kind: 'sign-in',
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
          return this.errorMessageService.invalidSignInRequest();
        case 401:
          return this.errorMessageService.invalidCredentials();
        case 403:
          return this.errorMessageService.disabledAccount();
        case 429:
          return this.errorMessageService.tooManyAttempts();
        default:
          return this.errorMessageService.serverError();
      }
    }
    return this.errorMessageService.serverError();
  }
}
