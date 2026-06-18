import client from './client';
import type { User } from '../types';

export const getMe = () => client.get<User>('/users/me');

export const updateMe = (data: { firstName: string; lastName: string }) =>
  client.put<User>('/users/me', data);
