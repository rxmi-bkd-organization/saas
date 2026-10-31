import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { User } from '../model/user.model';
import { UserHttp } from '../http/user.http';
import { SecurityHttp } from '../http/security.http';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly userHttpService = inject(UserHttp);
  private readonly securityHttp = inject(SecurityHttp);

  private readonly _user = signal<User | null>(null);

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() != null);
  readonly isAdmin = computed(() => this._user()?.role === 'ROLE_ADMIN');

  async fetchCurrentUser(): Promise<void> {
    try {
      const user = await firstValueFrom(this.userHttpService.me());
      this._user.set(user);
    } catch {
      this._user.set(null);
    }
  }

  async login(email: string, password: string): Promise<void> {
    const user = await firstValueFrom(this.securityHttp.login(email, password));
    this._user.set(user);
  }

  async enableUser(jwt: string): Promise<void> {
    await firstValueFrom(this.securityHttp.enableUser(jwt));
  }

  async sendForgottenPasswordEmail(email: string): Promise<void> {
    await firstValueFrom(this.securityHttp.sendForgottenPasswordEmail(email));
  }

  async resetPassword(jwt: string, newPassword: string): Promise<void> {
    await firstValueFrom(this.securityHttp.resetPassword(jwt, newPassword));
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.securityHttp.logout());
    } finally {
      this._user.set(null);
    }
  }
}
