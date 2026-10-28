export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export interface User {
  id: string;
  email: string;
  role: Role;
  createdAt: string;
  enabled: boolean;
}
