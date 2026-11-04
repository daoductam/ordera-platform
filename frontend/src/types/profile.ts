export type MemberTierName = "New" | "Silver" | "Gold" | "Platinum" | "Diamond" | "Ruby";

export interface TierMilestone {
  tier: MemberTierName;
  code: "N" | "S" | "G" | "P" | "D" | "R";
  requiredPoints: number;
  color: string;
  perks: string[];
}

export interface MemberProfile {
  id: string;
  name: string;
  phone: string;
  email: string;
  dateOfBirth?: string;
  avatarUrl?: string;
  tier: MemberTierName;
  currentPoints: number;
  lifetimePoints: number;
  nextTierPointsNeeded: number;
  nextTierName: MemberTierName;
  voucherCount: number;
  giftCount: number;
  ordersCount: number;
  unreadNotificationsCount: number;
  hasMissingPointsNotice?: boolean;
}

export interface UpdateProfileDto {
  name: string;
  email: string;
  phone: string;
  dateOfBirth?: string;
  password?: string;
  rememberMe?: boolean;
}

