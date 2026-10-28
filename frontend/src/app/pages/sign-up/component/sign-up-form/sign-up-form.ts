import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { email, form, maxLength, minLength, required } from '@angular/forms/signals';

import { UserService } from '../../../../core/service/user.service';
import { ROUTE_URLS } from '../../../../core/constant/route.constant';
import { PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from '../../../../core/constant/password.constant';
import { ErrorMessageService } from '../../../../core/service/error-message.service';
import { SignUpForm as SignUpFormValue } from '../../../../core/model/form/sign-up.form';
import { FormContainer } from '../../../../shared/ui/form-container/form-container';
import { FormField } from '../../../../shared/ui/form-field/form-field';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-sign-up-form',
  imports: [FormContainer, FormField, RouterLink, TranslocoPipe],
  templateUrl: './sign-up-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignUpForm {
  protected readonly ROUTE_URLS = ROUTE_URLS;

  private readonly router = inject(Router);
  private readonly userService = inject(UserService);

  private readonly errorMessageService = inject(ErrorMessageService);

  private readonly model = signal<SignUpFormValue>({ email: '', password: '' });

  protected readonly signUpForm = form(
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
      minLength(schema.password, PASSWORD_MIN_LENGTH, {
        message: () => this.errorMessageService.minLength('password', PASSWORD_MIN_LENGTH),
      });
      maxLength(schema.password, PASSWORD_MAX_LENGTH, {
        message: () => this.errorMessageService.maxLength('password', PASSWORD_MAX_LENGTH),
      });
    },
    {
      submission: {
        action: async () => {
          const { email, password } = this.model();
          try {
            await this.userService.createUser(email, password);
            await this.router.navigateByUrl(ROUTE_URLS.enableAccount);
            return undefined;
          } catch (error) {
            return {
              kind: 'sign-up',
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
          return this.errorMessageService.invalidSignUpRequest();
        case 409:
          return this.errorMessageService.emailAlreadyUsed();
        case 429:
          return this.errorMessageService.tooManyAttempts();
        default:
          return this.errorMessageService.serverError();
      }
    }
    return this.errorMessageService.serverError();
  }
}
