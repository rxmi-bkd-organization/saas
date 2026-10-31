import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuthenticationService } from '../../core/service/authentication.service';
import { LanguageService } from '../../core/service/language.service';
import { TranslocoPipe } from '@jsverse/transloco';
import { UpdatePasswordForm } from './component/update-password-form/update-password-form';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, TranslocoPipe, UpdatePasswordForm],
  templateUrl: './profile.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Profile {
  private readonly authenticationService = inject(AuthenticationService);
  protected readonly language = inject(LanguageService).getCurrentLanguageAsSignal();
  protected readonly user = this.authenticationService.user;
}
