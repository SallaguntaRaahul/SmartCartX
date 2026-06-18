import client from './client';
import type { PageResponse, Product } from '../types';

export const getProducts = (page = 0, size = 12, sortBy = 'id') =>
  client.get<PageResponse<Product>>('/products', {
    params: { page, size, sortBy },
  });

export const getProductsByCategory = (
  category: string,
  page = 0,
  size = 12
) =>
  client.get<PageResponse<Product>>(`/products/category/${category}`, {
    params: { page, size },
  });

export const searchProducts = (name: string, page = 0, size = 12) =>
  client.get<PageResponse<Product>>('/products/search', {
    params: { name, page, size },
  });

export const getProductById = (id: number) =>
  client.get<Product>(`/products/${id}`);
