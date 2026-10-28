import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';

import { form, maxLength, minLength, required } from '@angular/forms/signals';

import { UserService } from '../../../../core/service/user.service';
import { PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from '../../../../core/constant/password.constant';
import { ErrorMessageService } from '../../../../core/service/error-message.service';
import { UpdatePasswordForm as UpdatePasswordFormValue } from '../../../../core/model/form/update-password.form';
import { FormContainer } from '../../../../shared/ui/form-container/form-container';
import { FormField } from '../../../../shared/ui/form-field/form-field';
import { TranslocoPipe } from '@jsverse/transloco';

const EMPTY_FORM: UpdatePasswordFormValue = { oldPassword: '', newPassword: '' };

@Component({
  selector: 'app-update-password-form',
  imports: [FormContainer, FormField, TranslocoPipe],
  templateUrl: './update-password-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpdatePasswordForm {
  private readonly userService = inject(UserService);
  private readonly errorMessageService = inject(ErrorMessageService);

  private readonly model = signal<UpdatePasswordFormValue>({ ...EMPTY_FORM });

  protected readonly updated = signal(false);

  protected readonly updatePasswordForm = form(
    this.model,
    (schema) => {
      required(schema.oldPassword, {
        message: () => this.errorMessageService.required('password'),
      });
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
          this.updated.set(false);
          try {
            const { oldPassword, newPassword } = this.model();
            await this.userService.updateUserPassword(oldPassword, newPassword);
            this.updatePasswordForm().reset({ ...EMPTY_FORM });
            this.updated.set(true);
            return undefined;
          } catch (error) {
            return {
              kind: 'update-password',
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
          return this.errorMessageService.invalidUpdatePasswordRequest();
        case 429:
          return this.errorMessageService.tooManyAttempts();
        default:
          return this.errorMessageService.serverError();
      }
    }
    return this.errorMessageService.serverError();
  }
}
