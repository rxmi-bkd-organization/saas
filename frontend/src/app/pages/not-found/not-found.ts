import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Error } from '../../layout/error/error';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.html',
  imports: [Error, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFound {}
