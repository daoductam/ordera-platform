import type { CheckoutDetails } from "@/types/checkout";
import { MOCK_MY_VOUCHERS, MOCK_MY_GIFTS } from "./rewardMock";

export const DEFAULT_CHECKOUT_DETAILS: CheckoutDetails = {
  storeName: "Linn Sushi Sentrum",
  storeAddress: "Prinsens gate 10C, 0153 Oslo",
  items: [
    {
      id: "item-cart-1",
      name: "Laks Maki (8 biter)",
      quantity: 1,
      unitPrice: 150,
      totalPrice: 150,
      image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=400&q=80",
    },
    {
      id: "item-cart-2",
      name: "Tuna Maki (8 biter)",
      quantity: 1,
      unitPrice: 117,
      totalPrice: 117,
      image: "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=400&q=80",
    },
  ],
  subtotal: 267,
  tipSelection: "10Kr" as any,
  tipAmount: 10,
  appliedVoucher: null,
  appliedGift: null,
  discountAmount: 0,
  total: 277,
  pointsToEarn: 170,
  pickupMethod: "Counter",
  tableNumber: "",
  pickupTiming: "asap",
  preparingTime: "15 Minutes",
  customerName: "David Nguyen",
  customerPhone: "+47 97362748",
  agreedToLoyaltyTerms: true,
};

// Trạng thái đã áp dụng cả Voucher lẫn Quà (Figma 08.10)
export const DUAL_OFFERS_CHECKOUT_DETAILS: CheckoutDetails = {
  ...DEFAULT_CHECKOUT_DETAILS,
  appliedVoucher: MOCK_MY_VOUCHERS[1], // DISCOUNT30%
  appliedGift: MOCK_MY_GIFTS[0], // Cốc giữ nhiệt
  discountAmount: 80.1, // 30% of 267 Kr
  total: 196.9, // 267 - 80.1 + 10 tip
  pointsToEarn: 150,
};

