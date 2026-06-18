import client from './client';
import type { Cart } from '../types';

export const getCart = () => client.get<Cart>('/cart');

export const addToCart = (productId: number, quantity: number) =>
  client.post<Cart>('/cart/items', { productId, quantity });

export const updateCartItem = (itemId: number, quantity: number) =>
  client.put<Cart>(`/cart/items/${itemId}`, null, {
    params: { quantity },
  });

export const removeCartItem = (itemId: number) =>
  client.delete<Cart>(`/cart/items/${itemId}`);

export const clearCart = () => client.delete<Cart>('/cart');
