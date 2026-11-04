export type RewardType = "voucher" | "gift";

export interface VoucherItem {
  id: string;
  code: string;
  title: string;
  discountType: "fixed" | "percent";
  discountValue: number; // e.g. 100 NOK or 30%
  description: string;
  minOrderValue: number; // e.g. 250 NOK or 0 NOK
  expiryDate: string; // e.g. "3 dec 2025"
  pointsCost?: number; // For "Get More Rewards"
  isRedeemed?: boolean;
}

export interface LoyaltyGiftItem {
  id: string;
  title: string;
  description: string;
  tag: string;
  quantityLeft: number;
  pointsCost?: number; // e.g. 100 Points or 1000 Points
  image: string;
  isRedeemed?: boolean;
}

export interface PromoCodeResult {
  valid: boolean;
  code: string;
  voucher?: VoucherItem;
  message?: string;
}

