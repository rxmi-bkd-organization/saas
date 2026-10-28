import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ROUTE_URLS } from '../../core/constant/route.constant';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-email-sent',
  imports: [RouterLink, TranslocoPipe],
  templateUrl: './email-sent.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailSent {
  protected readonly ROUTE_URLS = ROUTE_URLS;
}
