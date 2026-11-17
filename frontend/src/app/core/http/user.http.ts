import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../model/user.model';
import { API, PUBLIC_API } from '../constant/api.constant';

const USERS_API = `${API}/users`;
const PUBLIC_USERS_API = `${PUBLIC_API}/users`;

@Injectable({ providedIn: 'root' })
export class UserHttp {
  private readonly http = inject(HttpClient);

  me(): Observable<User> {
    const url = `${USERS_API}/me`;
    return this.http.get<User>(url);
  }

  createUser(email: string, password: string): Observable<User> {
    const url = `${PUBLIC_USERS_API}`;
    const body = { email, password };
    return this.http.post<User>(url, body);
  }

  updateUserPassword(oldPassword: string, newPassword: string): Observable<void> {
    const url = `${USERS_API}/password`;
    const body = { oldPassword, newPassword };
    return this.http.put<void>(url, body);
  }
}
