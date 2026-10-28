import { inject, Injectable } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Injectable({ providedIn: 'root' })
export class ErrorMessageService {
  private readonly translationService = inject(TranslocoService);

  required = (field: string) => this.fieldError('error.required', field);
  invalid = (field: string) => this.fieldError('error.invalid', field);
  minLength = (field: string, length: number) => this.fieldError('error.minLength', field, length);
  maxLength = (field: string, length: number) => this.fieldError('error.maxLength', field, length);

  invalidCredentials = () => this.translationService.translate('error.invalidCredentials');
  invalidSignInRequest = () => this.translationService.translate('error.invalidSignInRequest');
  disabledAccount = () => this.translationService.translate('error.disabledAccount');
  tooManyAttempts = () => this.translationService.translate('error.tooManyAttempts');
  serverError = () => this.translationService.translate('error.serverError');
  networkError = () => this.translationService.translate('error.networkError');
  accountNotFound = () => this.translationService.translate('error.accountNotFound');
  forgotPasswordFailed = () => this.translationService.translate('error.forgotPasswordFailed');
  invalidResetPasswordRequest = () => this.translationService.translate('error.invalidResetPasswordRequest');
  invalidResetToken = () => this.translationService.translate('error.invalidResetToken');
  invalidSignUpRequest = () => this.translationService.translate('error.invalidSignUpRequest');
  emailAlreadyUsed = () => this.translationService.translate('error.emailAlreadyUsed');
  invalidUpdatePasswordRequest = () => this.translationService.translate('error.invalidUpdatePasswordRequest');

  private fieldError(key: string, field: string, length?: number): string {
    const params = { field: this.translationService.translate(`field.${field}`), length };
    return this.translationService.translate(key, params);
  }
}
