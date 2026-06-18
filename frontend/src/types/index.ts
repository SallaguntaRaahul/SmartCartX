export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  category: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  category: string;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalPrice: number;
  totalItems: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  category: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

export interface Order {
  id: number;
  status: 'PENDING' | 'CANCELLED' | 'COMPLETED' | string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
}

export interface AISummaryResponse {
  orderId: string;
  summary: string;
}

export interface AIAnomalyResponse {
  orderId: number;
  analysis: string;
}

export interface AIRecommendationResponse {
  cartItems: string;
  contextSource?: string;
  reason: string;
  products: Product[];
}
