import client from './client';
import type {
  AIAnomalyResponse,
  AIRecommendationResponse,
  AISummaryResponse,
} from '../types';

export const getOrderSummary = (orderId: number) =>
  client.get<AISummaryResponse>(`/ai/orders/${orderId}/summary`);

export const getOrderAnomaly = (orderId: number) =>
  client.get<AIAnomalyResponse>(`/ai/orders/${orderId}/anomaly`);

export const getRecommendations = () =>
  client.get<AIRecommendationResponse>('/ai/recommendations');
