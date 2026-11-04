export type OrderStatus = "preparing" | "ready" | "completed" | "cancelled";
export type OrderPickupMethod = "Takeaway" | "Dine In" | "Delivery";

export interface OrderDishItem {
  id: string;
  name: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
  image?: string;
  isGift?: boolean;
}

export interface CustomerOrder {
  id: string;
  orderNumber: string; // e.g. "#907", "#897", "#005"
  customerName: string; // e.g. "Naomi - #005", "David Nguyen"
  restaurantName: string; // e.g. "Shop Name 1"
  placedAt: string; // e.g. "17 mar 2025 , 17:01"
  status: OrderStatus;
  pickupMethod: OrderPickupMethod;
  tableNumber?: string;
  items: OrderDishItem[];
  subtotal: number;
  tipAmount: number;
  discountAmount: number;
  appliedVoucherCode?: string;
  appliedGiftName?: string;
  totalAmount: number;
  earnedPoints: number;
  eReceiptEmail?: string;
}

export interface EReceiptRequest {
  orderId: string;
  email: string;
}

