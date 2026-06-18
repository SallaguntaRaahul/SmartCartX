import client from './client';
import type { AuthResponse } from '../types';

export const login = (email: string, password: string) =>
  client.post<AuthResponse>('/auth/login', { email, password });

export const register = (data: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}) => client.post<AuthResponse>('/auth/register', data);
