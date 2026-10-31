import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../model/user.model';
import { API, PUBLIC_API } from '../constant/api.constant';

@Injectable({ providedIn: 'root' })
export class UserHttp {
  private readonly http = inject(HttpClient);

  me(): Observable<User> {
    const url = `${API}/users/me`;
    return this.http.get<User>(url);
  }

  createUser(email: string, password: string): Observable<User> {
    const url = `${PUBLIC_API}/users`;
    const body = { email, password };
    return this.http.post<User>(url, body);
  }

  updateUserPassword(oldPassword: string, newPassword: string): Observable<void> {
    const url = `${API}/users/password`;
    const body = { oldPassword, newPassword };
    return this.http.put<void>(url, body);
  }
}
