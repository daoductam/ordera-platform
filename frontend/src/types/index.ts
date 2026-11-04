// ==========================================
// 1. API RESPONSE ENVELOPES
// ==========================================
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface PaginationMeta {
  page: number;
  limit: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev?: boolean;
}

export interface ApiPaginatedResponse<T> {
  success: boolean;
  data: T[];
  meta: PaginationMeta;
  message?: string;
  timestamp: string;
}

export interface ApiErrorDetail {
  field?: string;
  message: string;
}

export interface ApiErrorPayload {
  code: string;
  message: string;
  details?: ApiErrorDetail[] | null;
}

export interface ApiErrorResponse {
  success: false;
  error: ApiErrorPayload;
  timestamp: string;
}

// ==========================================
// 2. LOYALTY & TIERS DTOs
// ==========================================
export type TierCode = 'NEW' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND' | 'RUBY';

export interface TierLevelDto {
  code: TierCode;
  name: string;
  minPoints: number;
  maxPoints: number;
  earnRate: number;
  colorHex: string;
  gradient: string;
  perks: string[];
}

export interface NextTierProgressDto {
  code: TierCode;
  name: string;
  pointsRequired: number;
  pointsRemaining: number;
  progressPercentage: number;
}

export interface MemberProfileDto {
  id: string;
  userId: string;
  fullName: string;
  phone: string;
  email?: string;
  dob?: string;
  currentPoints: number;
  lifetimePoints: number;
  tier: TierLevelDto;
  nextTier?: NextTierProgressDto;
}

export type PointsTransactionType = 'EARN' | 'REDEEM' | 'EXPIRE' | 'ADJUST_ADD' | 'ADJUST_SUB';

export interface PointsLedgerEntryDto {
  id: string;
  transactionType: PointsTransactionType;
  pointsDelta: number;
  balanceAfter: number;
  referenceId?: string;
  note: string;
  createdAt: string;
}

// ==========================================
// 3. REWARDS & VOUCHERS DTOs
// ==========================================
export type DiscountType = 'PERCENT' | 'FIXED_AMOUNT' | 'FREE_ITEM';

export interface VoucherDto {
  id: string;
  code: string;
  title: string;
  description?: string;
  discountType: DiscountType;
  discountValue: number;
  pointsCost: number;
  minOrderValue: number;
  validFrom: string;
  validTo: string;
  status?: 'ACTIVE' | 'USED' | 'EXPIRED';
  imageUrl?: string;
}

// ==========================================
// 4. STORES, MENU & ORDERS DTOs
// ==========================================
export interface StoreDto {
  id: string;
  name: string;
  address: string;
  distanceKm?: number;
  phone: string;
  isOpen: boolean;
}

export interface ProductDto {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  category: string;
  description: string;
  image: string;
  isPopular?: boolean;
}

export interface CartItemDto {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  image?: string;
  selectedOptions?: string[];
  note?: string;
}

export interface CheckoutRequestDto {
  storeId: string;
  orderType: 'TAKEAWAY' | 'DELIVERY' | 'KIOSK';
  items: CartItemDto[];
  voucherCode?: string;
  tipAmount?: number;
  customerPhone?: string;
  customerName?: string;
}

export interface OrderResultDto {
  orderId: string;
  orderCode: string;
  subtotal: number;
  discountAmount: number;
  tipAmount: number;
  totalAmount: number;
  pointsEarned: number;
  status: string;
  createdAt: string;
  storeName: string;
}

export interface NotificationDto {
  id: string;
  title: string;
  message: string;
  type: 'POINT_EARNED' | 'TIER_UPGRADE' | 'PROMO' | 'SYSTEM';
  isRead: boolean;
  createdAt: string;
}

export * from './profile';
export * from './notification';
export * from './order';
export * from './reward';
export * from './checkout';
export * from './auth';
export * from './store-discovery';


