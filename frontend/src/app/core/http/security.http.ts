import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../model/user.model';
import { PUBLIC_API } from '../constant/api.constant';

@Injectable({ providedIn: 'root' })
export class SecurityHttp {
  private readonly http = inject(HttpClient);

  login(email: string, password: string): Observable<User> {
    const url = `${PUBLIC_API}/security/login`;
    const body = { email, password };
    return this.http.post<User>(url, body);
  }

  logout(): Observable<void> {
    const url = `${PUBLIC_API}/security/logout`;
    return this.http.post<void>(url, null);
  }

  enableUser(jwt: string): Observable<void> {
    const url = `${PUBLIC_API}/enable-user`;
    const body = { jwt };
    return this.http.post<void>(url, body);
  }

  sendActivationEmail(email: string): Observable<void> {
    const url = `${PUBLIC_API}/security/send-activation-email`;
    const body = { email };
    return this.http.post<void>(url, body);
  }

  sendForgottenPasswordEmail(email: string): Observable<void> {
    const url = `${PUBLIC_API}/security/send-forgotten-password-email`;
    const body = { email };
    return this.http.post<void>(url, body);
  }

  resetPassword(jwt: string, newPassword: string): Observable<void> {
    const url = `${PUBLIC_API}/security/reset-password`;
    const body = { jwt, newPassword };
    return this.http.post<void>(url, body);
  }
}
