import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthenticationService } from '../../core/service/authentication.service';
import { ROUTE_URLS } from '../../core/constant/route.constant';
import { TranslocoPipe } from '@jsverse/transloco';
import { LanguageSelector } from './language-selector/language-selector';

@Component({
  selector: 'app-header',
  imports: [RouterLink, TranslocoPipe, LanguageSelector],
  templateUrl: './header.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Header {
  private readonly authenticationService = inject(AuthenticationService);

  protected readonly isAdmin = this.authenticationService.isAdmin;
  protected readonly isAuthenticated = this.authenticationService.isAuthenticated;

  protected readonly ROUTE_URLS = ROUTE_URLS;

  protected async logout(): Promise<void> {
    await this.authenticationService.logout();
  }
}
