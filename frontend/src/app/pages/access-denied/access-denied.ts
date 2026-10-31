import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Error } from '../../layout/error/error';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-access-denied',
  templateUrl: './access-denied.html',
  imports: [Error, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessDenied {}
