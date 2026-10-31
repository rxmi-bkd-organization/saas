import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ROUTE_URLS } from '../../core/constant/route.constant';
import { AuthenticationService } from '../../core/service/authentication.service';
import { TranslocoPipe } from '@jsverse/transloco';

type EnableAccountState = 'pending' | 'loading' | 'enabled' | 'invalid' | 'error';

@Component({
  selector: 'app-enable-account',
  imports: [RouterLink, TranslocoPipe],
  templateUrl: './enable-account.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnableAccount implements OnInit {
  protected readonly ROUTE_URLS = ROUTE_URLS;

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authenticationService = inject(AuthenticationService);

  protected readonly state = signal<EnableAccountState>('pending');

  async ngOnInit(): Promise<void> {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      try {
        await this.router.navigate([], { replaceUrl: true, queryParams: {} });
        this.state.set('loading');
        await this.authenticationService.enableUser(token);
        this.state.set('enabled');
      } catch (error) {
        this.state.set(this.mapErrorToState(error));
      }
    }
  }

  private mapErrorToState(error: unknown): EnableAccountState {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 409:
          return 'enabled';
        case 400:
        case 401:
        case 404:
          return 'invalid';
      }
    }
    return 'error';
  }
}
