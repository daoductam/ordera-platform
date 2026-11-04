import type { VoucherItem, LoyaltyGiftItem } from "@/types/reward";

// Vouchers người dùng đang sở hữu (Figma 07.03 My Rewards & 08.04 Checkout Offers)
export const MOCK_MY_VOUCHERS: VoucherItem[] = [
  {
    id: "vouch-1",
    code: "VOUCHER100",
    title: "Voucher Name",
    discountType: "fixed",
    discountValue: 100, // 100 NOK
    description: "Giảm 100 NOK giá trị cho Order của bạn",
    minOrderValue: 250,
    expiryDate: "3 dec 2025",
    isRedeemed: true,
  },
  {
    id: "vouch-2",
    code: "DISCOUNT30%",
    title: "-30 %",
    discountType: "percent",
    discountValue: 30, // 30%
    description: "Giảm 30% giá trị cho đơn hàng của bạn",
    minOrderValue: 0,
    expiryDate: "3 dec 2025",
    isRedeemed: true,
  },
];

// Quà tặng thành viên người dùng đang có (Figma 07.03 & 08.10)
export const MOCK_MY_GIFTS: LoyaltyGiftItem[] = [
  {
    id: "gift-1",
    title: "Cốc giữ nhiệt",
    description: "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
    tag: "Tag",
    quantityLeft: 10,
    pointsCost: 0,
    image: "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=400&q=80",
    isRedeemed: true,
  },
  {
    id: "gift-2",
    title: "Bánh Mochi Trà Xanh",
    description: "Tặng kèm 01 phần bánh Mochi Nhật Bản khi dùng bữa tại nhà hàng",
    tag: "Quà tặng",
    quantityLeft: 5,
    pointsCost: 0,
    image: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80",
    isRedeemed: true,
  },
];

// Danh sách phần thưởng có thể đổi bằng điểm tích lũy (Figma 07.04 Get More Rewards)
export const MOCK_REDEEMABLE_REWARDS: (VoucherItem | LoyaltyGiftItem)[] = [
  {
    id: "redeem-vouch-1",
    code: "VOUCHER100_REDEEM",
    title: "Voucher Name",
    discountType: "fixed",
    discountValue: 100,
    description: "Giảm 100 NOK giá trị cho Order của bạn",
    minOrderValue: 250,
    expiryDate: "3 dec 2025",
    pointsCost: 100, // 100 Points
  } as VoucherItem,
  {
    id: "redeem-vouch-2",
    code: "DISCOUNT30%_REDEEM",
    title: "-30 %",
    discountType: "percent",
    discountValue: 30,
    description: "Giảm 30% giá trị cho đơn hàng của bạn",
    minOrderValue: 0,
    expiryDate: "3 dec 2025",
    pointsCost: 1000, // 1000 Points
  } as VoucherItem,
  {
    id: "redeem-gift-1",
    title: "Gift Name",
    description: "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
    tag: "Tag",
    quantityLeft: 10,
    pointsCost: 100,
    image: "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=400&q=80",
  } as LoyaltyGiftItem,
];

