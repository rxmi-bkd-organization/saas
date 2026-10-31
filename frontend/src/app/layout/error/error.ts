import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ROUTE_URLS } from '../../core/constant/route.constant';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-error',
  templateUrl: './error.html',
  imports: [RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Error {
  readonly code = input.required<string>();
  readonly heading = input.required<string>();

  protected readonly ROUTE_URLS = ROUTE_URLS;
}
