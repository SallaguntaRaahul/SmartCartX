import client from './client';
import type { Order, PageResponse } from '../types';

export const placeOrder = () => client.post<Order>('/orders');

export const getMyOrders = (page = 0, size = 10) =>
  client.get<PageResponse<Order>>('/orders', { params: { page, size } });

export const getOrderById = (id: number) =>
  client.get<Order>(`/orders/${id}`);

export const cancelOrder = (id: number) =>
  client.put<Order>(`/orders/${id}/cancel`);
