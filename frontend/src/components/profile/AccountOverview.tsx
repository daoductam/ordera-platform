import React from "react";
import {
  ChevronLeft,
  Bell,
  User,
  Ticket,
  Gift,
  ShoppingBag,
  ChevronRight,
  Settings,
  HelpCircle,
  FileText,
  UserCheck,
} from "lucide-react";
import type { MemberProfile, MemberTierName } from "@/types/profile";
import { MemberCard } from "./MemberCard";
import { MilestoneStepper } from "./MilestoneStepper";
import { MissingPointsBanner } from "@/components/notifications/MissingPointsBanner";

export interface AccountOverviewProps {
  profile: MemberProfile;
  onBack: () => void;
  onOpenNotifications: () => void;
  onOpenProfileDetail: () => void;
  onOpenVouchers: () => void;
  onOpenGifts: () => void;
  onOpenOrders: () => void;
  onOpenTierModal: (tier: MemberTierName) => void;
  onSelectTier?: (tier: MemberTierName) => void;
  onLearnMoreMissingPoints: () => void;
  onLogout?: () => void;
}

export const AccountOverview: React.FC<AccountOverviewProps> = ({
  profile,
  onBack,
  onOpenNotifications,
  onOpenProfileDetail,
  onOpenVouchers,
  onOpenGifts,
  onOpenOrders,
  onOpenTierModal,
  onSelectTier,
  onLearnMoreMissingPoints,
  onLogout,
}) => {
  return (
    <div className="bg-white min-h-screen pb-24 text-slate-900 font-sans">
      {/* 1. TOP HEADER (Figma: < David Nguyen • 🔔 • 👤) */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 text-slate-900 hover:text-slate-700 active:scale-95 transition-all"
        >
          <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
          <span className="font-black text-sm tracking-tight">{profile.name}</span>
        </button>

        <div className="flex items-center gap-2">
          {/* Nút Chuông Thông Báo với Badge */}
          <button
            onClick={onOpenNotifications}
            title="Notifications"
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-700 transition-all relative"
          >
            <Bell className="w-4 h-4" />
            {profile.unreadNotificationsCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-red-500 text-white text-[10px] font-black flex items-center justify-center shadow-xs">
                {profile.unreadNotificationsCount}
              </span>
            )}
          </button>

          {/* Nút Avatar Cá Nhân */}
          <button
            onClick={onOpenProfileDetail}
            title="Profile Detail"
            className="w-7 h-7 rounded-full overflow-hidden ring-2 ring-pink-500/80 active:scale-95 flex items-center justify-center bg-slate-800 text-white transition-all shadow-xs"
          >
            {profile.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                alt={profile.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-3.5 h-3.5" />
            )}
          </button>
        </div>
      </header>

      <div className="p-4 space-y-4">
        {/* BANNER THIẾU ĐIỂM (Figma 05.01) */}
        {profile.hasMissingPointsNotice && (
          <MissingPointsBanner onLearnMore={onLearnMoreMissingPoints} />
        )}

        {/* 2. THẺ HẠNG THÀNH VIÊN (Figma 04.01 / 04.02 / 04.10 - 04.15) */}
        <MemberCard
          tier={profile.tier}
          currentPoints={profile.currentPoints}
          onClick={() => onOpenTierModal(profile.tier)}
        />

        {/* 3. BỘ ĐẾM MỐC THĂNG HẠNG (Milestone Stepper 6 mốc N-S-G-P-D-R) */}
        <MilestoneStepper
          currentTier={profile.tier}
          lifetimePoints={profile.lifetimePoints}
          nextTierPointsNeeded={profile.nextTierPointsNeeded}
          onSelectTier={onSelectTier || onOpenTierModal}
        />

        {/* 4. CỤM 3 NÚT NHANH: Voucher • Loyalty Gift • Orders (Figma 06.01, 07.01, 07.02) */}
        <div className="grid grid-cols-3 gap-2.5 pt-1">
          {/* Nút Voucher */}
          <button
            onClick={onOpenVouchers}
            className="bg-white rounded-2xl p-3 border border-slate-200 shadow-2xs hover:border-amber-400 active:scale-95 flex flex-col items-center justify-center text-center transition-all relative group"
          >
            {profile.voucherCount > 0 && (
              <span className="absolute top-2 right-2 px-1.5 py-0.5 rounded-full bg-amber-500 text-slate-950 text-[10px] font-black shadow-xs">
                {profile.voucherCount}
              </span>
            )}
            <div className="w-9 h-9 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center mb-1 group-hover:scale-105 transition-transform">
              <Ticket className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold text-slate-800">Voucher</span>
          </button>

          {/* Nút Loyalty Gift */}
          <button
            onClick={onOpenGifts}
            className="bg-white rounded-2xl p-3 border border-slate-200 shadow-2xs hover:border-amber-400 active:scale-95 flex flex-col items-center justify-center text-center transition-all relative group"
          >
            {profile.giftCount > 0 && (
              <span className="absolute top-2 right-2 px-1.5 py-0.5 rounded-full bg-amber-500 text-slate-950 text-[10px] font-black shadow-xs">
                {profile.giftCount}
              </span>
            )}
            <div className="w-9 h-9 rounded-xl bg-orange-50 text-orange-700 flex items-center justify-center mb-1 group-hover:scale-105 transition-transform">
              <Gift className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold text-slate-800">Loyalty Gift</span>
          </button>

          {/* Nút Orders */}
          <button
            onClick={onOpenOrders}
            className="bg-white rounded-2xl p-3 border border-slate-200 shadow-2xs hover:border-amber-400 active:scale-95 flex flex-col items-center justify-center text-center transition-all relative group"
          >
            {profile.ordersCount > 0 && (
              <span className="absolute top-2 right-2 px-1.5 py-0.5 rounded-full bg-slate-900 text-white text-[10px] font-black shadow-xs">
                {profile.ordersCount}
              </span>
            )}
            <div className="w-9 h-9 rounded-xl bg-slate-100 text-slate-700 flex items-center justify-center mb-1 group-hover:scale-105 transition-transform">
              <ShoppingBag className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold text-slate-800">Orders</span>
          </button>
        </div>

        {/* 5. DANH SÁCH MỤC CÀI ĐẶT & THÔNG TIN CHUẨN FIGMA */}
        <div className="bg-white rounded-2xl border border-slate-100 divide-y divide-slate-100 shadow-2xs overflow-hidden pt-1">
          {/* Thông tin cá nhân */}
          <button
            onClick={onOpenProfileDetail}
            className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600">
                <UserCheck className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-slate-800">Personal Infomation</span>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-400" />
          </button>

          {/* Cài đặt */}
          <button
            onClick={() => {}}
            className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600">
                <Settings className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-slate-800">Setting</span>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-400" />
          </button>

          {/* Trợ giúp */}
          <button
            onClick={onLearnMoreMissingPoints}
            className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600">
                <HelpCircle className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-slate-800">Help</span>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-400" />
          </button>

          {/* Điều khoản & chính sách */}
          <button
            onClick={() => {}}
            className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600">
                <FileText className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-slate-800">Terms & Policies</span>
            </div>
            <ChevronRight className="w-4 h-4 text-slate-400" />
          </button>
        </div>

        {/* 6. NÚT ĐĂNG XUẤT (Figma 04.06: Menu tài khoản · Mở rộng · Hiển thị thêm lựa chọn đăng xuất) */}
        <div className="pt-2">
          <button
            onClick={onLogout}
            className="w-full py-3 px-4 rounded-lg border border-slate-200 bg-white text-slate-900 font-bold text-sm text-center shadow-2xs hover:bg-slate-50 hover:border-slate-300 active:scale-[0.99] transition-all"
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  );
};

