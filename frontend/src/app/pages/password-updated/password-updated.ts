import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ROUTE_URLS } from '../../core/constant/route.constant';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-password-updated',
  imports: [RouterLink, TranslocoPipe],
  templateUrl: './password-updated.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasswordUpdated {
  protected readonly ROUTE_URLS = ROUTE_URLS;
}
