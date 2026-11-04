export type NotificationCategory = "Messages" | "News" | "Voucher" | "Loyalty Gifts";

export interface SystemNotification {
  id: string;
  category: NotificationCategory;
  title: string;
  summary: string;
  detail: string;
  date: string;
  isRead: boolean;
  type: "missing_points" | "system_upgrade" | "promo_gift" | "order_status";
  actionLabel?: string;
  actionUrl?: string;
  meta?: {
    requiredOrderValue?: string;
    pointsCost?: number;
    supportEmail?: string;
  };
}

export interface SupportInquiryDto {
  email: string;
  phone: string;
  message: string;
  previousEmailOrPhone?: string;
  agreedToTerms: boolean;
}

