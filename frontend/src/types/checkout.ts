import type { VoucherItem, LoyaltyGiftItem } from "./reward";
import type { OrderDishItem } from "./order";

export type TipOption = "none" | "5%" | "10%" | "15%" | "roundup" | "20Kr" | "30Kr";

export interface CheckoutDetails {
  storeName: string;
  storeAddress: string;
  items: OrderDishItem[];
  subtotal: number;
  tipSelection: TipOption;
  tipAmount: number;
  appliedVoucher?: VoucherItem | null;
  appliedGift?: LoyaltyGiftItem | null;
  discountAmount: number;
  total: number;
  pointsToEarn: number;
  pickupMethod: "Counter" | "Table";
  tableNumber?: string;
  pickupTiming: "asap" | "schedule";
  scheduledTime?: string;
  preparingTime: string;
  customerName?: string;
  customerPhone?: string;
  agreedToLoyaltyTerms?: boolean;
}

