import type { MemberProfile, TierMilestone } from "@/types/profile";

export const MOCK_TIER_MILESTONES: TierMilestone[] = [
  {
    tier: "New",
    code: "N",
    requiredPoints: 0,
    color: "#1EB759",
    perks: [
      "Tích lũy 10 điểm cho mỗi 100 NOK chi tiêu",
      "Quà tặng chào mừng thành viên mới",
      "Truy cập các voucher khuyến mãi độc quyền",
    ],
  },
  {
    tier: "Silver",
    code: "S",
    requiredPoints: 200,
    color: "#00A3FF",
    perks: [
      "Tích lũy 12 điểm cho mỗi 100 NOK chi tiêu (+20%)",
      "Voucher giảm giá 10% cho tháng sinh nhật",
      "Miễn phí nâng size 01 món đồ uống mỗi tháng",
    ],
  },
  {
    tier: "Gold",
    code: "G",
    requiredPoints: 400,
    color: "#F5B800",
    perks: [
      "Tích lũy 15 điểm cho mỗi 100 NOK chi tiêu (+50%)",
      "Voucher sinh nhật trị giá 100 NOK",
      "Ưu tiên nhận món vào giờ cao điểm",
      "Miễn phí 01 phần quà tặng hàng quý",
    ],
  },
  {
    tier: "Platinum",
    code: "P",
    requiredPoints: 1000,
    color: "#FF5B4D",
    perks: [
      "Tích lũy 18 điểm cho mỗi 100 NOK chi tiêu (+80%)",
      "Voucher giảm 20% đơn hàng tối đa 200 NOK",
      "Quà tặng độc quyền từ các thương hiệu đối tác",
      "Chăm sóc khách hàng VIP riêng",
    ],
  },
  {
    tier: "Diamond",
    code: "D",
    requiredPoints: 2000,
    color: "#7C3AED",
    perks: [
      "Tích lũy 20 điểm cho mỗi 100 NOK chi tiêu (x2 điểm)",
      "Set quà đặc biệt sinh nhật",
      "Vé mời tham gia sự kiện ẩm thực hàng năm",
      "Quyền đổi thưởng không giới hạn số lượng",
    ],
  },
  {
    tier: "Ruby",
    code: "R",
    requiredPoints: 4000,
    color: "#D9148E",
    perks: [
      "Tích lũy 25 điểm cho mỗi 100 NOK chi tiêu (x2.5 điểm)",
      "Trải nghiệm menu mới trước ngày ra mắt miễn phí",
      "Đặc quyền phòng riêng và bàn danh dự",
      "Bộ sưu tập quà tặng biểu tượng phiên bản giới hạn",
    ],
  },
];

// Profile Figma 04.01: ABY TRAN (Thành viên mới - New)
export const MOCK_NEW_PROFILE: MemberProfile = {
  id: "user-new",
  name: "ABY TRAN",
  phone: "+47 98765432",
  email: "aby.tran@ordera.no",
  dateOfBirth: "15/06/2002",
  tier: "New",
  currentPoints: 0,
  lifetimePoints: 0,
  nextTierPointsNeeded: 200,
  nextTierName: "Silver",
  voucherCount: 0,
  giftCount: 0,
  ordersCount: 0,
  unreadNotificationsCount: 1,
  hasMissingPointsNotice: true,
};

// Profile Figma 04.02 & 04.03: David Nguyen (Silver)
export const MOCK_SILVER_PROFILE: MemberProfile = {
  id: "user-silver",
  name: "David Nguyen",
  phone: "+47 97362748",
  email: "info@ordera.no",
  dateOfBirth: "23/08/2000",
  tier: "Silver",
  currentPoints: 120,
  lifetimePoints: 300,
  nextTierPointsNeeded: 100,
  nextTierName: "Gold",
  voucherCount: 2,
  giftCount: 1,
  ordersCount: 3,
  unreadNotificationsCount: 2,
  hasMissingPointsNotice: false,
  avatarUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=240&auto=format&fit=crop&q=80",
};

// Profiles cho 6 hạng để Switcher/Stepper chuyển đổi mượt mà khớp 100% Figma 04.10 - 04.15
export const MOCK_TIER_PROFILES: Record<string, MemberProfile> = {
  New: MOCK_NEW_PROFILE,
  Silver: MOCK_SILVER_PROFILE,
  Gold: {
    ...MOCK_SILVER_PROFILE,
    name: "David Nguyen",
    tier: "Gold",
    currentPoints: 120,
    lifetimePoints: 620,
    nextTierPointsNeeded: 380,
    nextTierName: "Platinum",
    voucherCount: 2,
    giftCount: 1,
    ordersCount: 3,
  },
  Platinum: {
    ...MOCK_SILVER_PROFILE,
    name: "David Nguyen",
    tier: "Platinum",
    currentPoints: 120,
    lifetimePoints: 1820,
    nextTierPointsNeeded: 180,
    nextTierName: "Diamond",
    voucherCount: 2,
    giftCount: 1,
    ordersCount: 3,
  },
  Diamond: {
    ...MOCK_SILVER_PROFILE,
    name: "David Nguyen",
    tier: "Diamond",
    currentPoints: 120,
    lifetimePoints: 2000,
    nextTierPointsNeeded: 2000,
    nextTierName: "Ruby",
    voucherCount: 2,
    giftCount: 1,
    ordersCount: 3,
  },
  Ruby: {
    ...MOCK_SILVER_PROFILE,
    name: "David Nguyen",
    tier: "Ruby",
    currentPoints: 120,
    lifetimePoints: 6020,
    nextTierPointsNeeded: 0,
    nextTierName: "Ruby",
    voucherCount: 2,
    giftCount: 1,
    ordersCount: 3,
  },
};

