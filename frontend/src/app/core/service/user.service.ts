import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { UserHttp } from '../http/user.http';
import { User } from '../model/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly userHttp = inject(UserHttp);

  async createUser(email: string, password: string): Promise<User> {
    return await firstValueFrom(this.userHttp.createUser(email, password));
  }

  async updateUserPassword(oldPassword: string, newPassword: string): Promise<void> {
    await firstValueFrom(this.userHttp.updateUserPassword(oldPassword, newPassword));
  }
}
