import type {
  ApiResponse,
  ApiPaginatedResponse,
  MemberProfileDto,
  TierLevelDto,
  PointsLedgerEntryDto,
  StoreDto,
  ProductDto,
  VoucherDto,
  NotificationDto,
  CheckoutRequestDto,
  OrderResultDto,
} from "@/types";
import {
  MOCK_CURRENT_USER,
  MOCK_TIERS,
  MOCK_LEDGER_ENTRIES,
  MOCK_STORES,
  MOCK_PRODUCTS,
  MOCK_MY_VOUCHERS,
  MOCK_REWARD_CATALOG,
  MOCK_NOTIFICATIONS,
} from "@/mock/mockData";

// Giả lập độ trễ mạng thực tế (Network Latency)
const delay = (ms = 250) => new Promise((resolve) => setTimeout(resolve, ms));

export const apiService = {
  // 1. Hồ sơ thành viên & Hạng thẻ
  async getProfile(): Promise<ApiResponse<MemberProfileDto>> {
    await delay();
    return {
      success: true,
      data: MOCK_CURRENT_USER,
      message: "Lấy thông tin thành viên thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async getTiers(): Promise<ApiResponse<TierLevelDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_TIERS,
      message: "Lấy danh sách 6 hạng thẻ thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async getPointsLedger(): Promise<ApiPaginatedResponse<PointsLedgerEntryDto>> {
    await delay();
    return {
      success: true,
      data: MOCK_LEDGER_ENTRIES,
      meta: {
        page: 1,
        limit: 10,
        totalItems: MOCK_LEDGER_ENTRIES.length,
        totalPages: 1,
        hasNext: false,
      },
      message: "Lấy lịch sử biến động điểm thành công",
      timestamp: new Date().toISOString(),
    };
  },

  // 2. Cửa hàng & Thực đơn
  async getStores(): Promise<ApiResponse<StoreDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_STORES,
      message: "Lấy danh sách cửa hàng thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async getProducts(): Promise<ApiResponse<ProductDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_PRODUCTS,
      message: "Lấy danh sách thực đơn thành công",
      timestamp: new Date().toISOString(),
    };
  },

  // 3. Ưu đãi & Đổi thưởng
  async getMyVouchers(): Promise<ApiResponse<VoucherDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_MY_VOUCHERS,
      message: "Lấy kho voucher thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async getRewardCatalog(): Promise<ApiResponse<VoucherDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_REWARD_CATALOG,
      message: "Lấy danh mục quà đổi điểm thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async redeemReward(voucherId: string): Promise<ApiResponse<VoucherDto>> {
    await delay(400);
    const reward = MOCK_REWARD_CATALOG.find((r) => r.id === voucherId);
    if (!reward) {
      throw new Error("Không tìm thấy quà đổi thưởng");
    }
    if (MOCK_CURRENT_USER.currentPoints < reward.pointsCost) {
      throw new Error("Số dư điểm khả dụng không đủ để đổi ưu đãi này");
    }

    // Trừ điểm giả lập
    MOCK_CURRENT_USER.currentPoints -= reward.pointsCost;

    const newVoucher: VoucherDto = {
      ...reward,
      id: `v_user_${Date.now()}`,
      status: "ACTIVE",
    };
    MOCK_MY_VOUCHERS.unshift(newVoucher);

    return {
      success: true,
      data: newVoucher,
      message: `Đổi thành công mã ${reward.title}`,
      timestamp: new Date().toISOString(),
    };
  },

  // 4. Giỏ hàng & Checkout
  async validateVoucher(code: string, subtotal: number): Promise<ApiResponse<{ isValid: boolean; discountAmount: number }>> {
    await delay();
    const voucher = MOCK_MY_VOUCHERS.find((v) => v.code.toUpperCase() === code.toUpperCase() && v.status === "ACTIVE");
    if (!voucher) {
      return {
        success: false,
        data: { isValid: false, discountAmount: 0 },
        message: "Mã ưu đãi không tồn tại hoặc đã sử dụng",
        timestamp: new Date().toISOString(),
      };
    }

    if (subtotal < voucher.minOrderValue) {
      return {
        success: false,
        data: { isValid: false, discountAmount: 0 },
        message: `Đơn hàng tối thiểu phải từ ${voucher.minOrderValue.toLocaleString()}đ để áp dụng mã này`,
        timestamp: new Date().toISOString(),
      };
    }

    return {
      success: true,
      data: {
        isValid: true,
        discountAmount: voucher.discountValue,
      },
      message: "Áp dụng mã ưu đãi thành công",
      timestamp: new Date().toISOString(),
    };
  },

  async submitOrder(req: CheckoutRequestDto): Promise<ApiResponse<OrderResultDto>> {
    await delay(500);
    const subtotal = req.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const discountAmount = req.voucherCode ? 50000 : 0;
    const tipAmount = req.tipAmount || 0;
    const totalAmount = Math.max(0, subtotal - discountAmount + tipAmount);
    const pointsEarned = Math.round((totalAmount * MOCK_CURRENT_USER.tier.earnRate) / 100);

    // Cộng điểm tích lũy
    MOCK_CURRENT_USER.currentPoints += pointsEarned;
    MOCK_CURRENT_USER.lifetimePoints += pointsEarned;

    return {
      success: true,
      data: {
        orderId: `ord_${Date.now()}`,
        orderCode: `#ORD-${Math.floor(1000 + Math.random() * 9000)}`,
        subtotal,
        discountAmount,
        tipAmount,
        totalAmount,
        pointsEarned,
        status: "COMPLETED",
        createdAt: new Date().toLocaleString("vi-VN"),
        storeName: "Ordera Vincom Đồng Khởi",
      },
      message: "Đặt đơn hàng thành công",
      timestamp: new Date().toISOString(),
    };
  },

  // 5. Thông báo
  async getNotifications(): Promise<ApiResponse<NotificationDto[]>> {
    await delay();
    return {
      success: true,
      data: MOCK_NOTIFICATIONS,
      message: "Lấy thông báo thành công",
      timestamp: new Date().toISOString(),
    };
  },
};
