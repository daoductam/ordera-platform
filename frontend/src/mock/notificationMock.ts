import type { SystemNotification } from "@/types/notification";

export const MOCK_NOTIFICATIONS: SystemNotification[] = [
  {
    id: "notif-1",
    category: "Messages",
    title: "Missing Your Points?",
    summary:
      "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!",
    detail:
      "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!\n\nContact us: Email: info@example.com\nHotline: (+47) 22 33 44 55\n\nOur support team will verify your phone number and historical receipts to credit all accumulated points within 24 hours.",
    date: "Hôm nay, 10:30",
    isRead: false,
    type: "missing_points",
    actionLabel: "Learn more",
    meta: {
      supportEmail: "info@example.com",
    },
  },
  {
    id: "notif-2",
    category: "News",
    title: "Chào mừng nâng cấp Ordera Loyalty 2.0",
    summary: "Hệ thống tích điểm thành viên mới đã chính thức hoạt động với nhiều đặc quyền hấp dẫn.",
    detail: "Khám phá giao diện mới, tiến trình thăng hạng 6 cấp độ từ New đến Ruby và nhận ngay các ưu đãi độc quyền khi dùng bữa tại các chi nhánh.",
    date: "Hôm qua, 15:00",
    isRead: true,
    type: "system_upgrade",
    actionLabel: "Xem chi tiết",
  },
  {
    id: "notif-3",
    category: "Voucher",
    title: "Voucher 100 NOK đã sẵn sàng trong tài khoản",
    summary: "Áp dụng giảm trực tiếp 100 NOK cho đơn hàng tối thiểu 250 NOK.",
    detail: "Voucher có hạn dùng đến 03/12/2025. Bạn có thể sử dụng trực tiếp tại bước thanh toán Checkout.",
    date: "17 Mar 2025",
    isRead: true,
    type: "promo_gift",
    actionLabel: "Dùng ngay",
    meta: {
      requiredOrderValue: "250 NOK",
    },
  },
  {
    id: "notif-4",
    category: "Loyalty Gifts",
    title: "Món quà thành viên mới: Cốc giữ nhiệt",
    summary: "Bạn có 01 quà tặng tri ân có thể nhận tại quầy hoặc thêm vào đơn hàng.",
    detail: "Món quà phiên bản giới hạn kỷ niệm mùa hè 2025 từ thương hiệu.",
    date: "15 Mar 2025",
    isRead: true,
    type: "promo_gift",
    actionLabel: "Đổi quà",
    meta: {
      pointsCost: 100,
    },
  },
];

