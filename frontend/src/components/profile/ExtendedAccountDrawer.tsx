import React from "react";
import { X, LogOut, ChevronRight, UserCheck, Settings, HelpCircle, FileText } from "lucide-react";
import type { MemberProfile } from "@/types/profile";
import { MemberCard } from "./MemberCard";
import { MilestoneStepper } from "./MilestoneStepper";

export interface ExtendedAccountDrawerProps {
  isOpen: boolean;
  profile: MemberProfile;
  onClose: () => void;
  onOpenProfileDetail: () => void;
  onOpenVouchers: () => void;
  onOpenGifts: () => void;
  onOpenOrders: () => void;
  onLogout: () => void;
}

export const ExtendedAccountDrawer: React.FC<ExtendedAccountDrawerProps> = ({
  isOpen,
  profile,
  onClose,
  onOpenProfileDetail,
  onOpenVouchers,
  onOpenGifts,
  onOpenOrders,
  onLogout,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white h-full shadow-2xl flex flex-col overflow-y-auto animate-in slide-in-from-right duration-300">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between sticky top-0 bg-white/95 backdrop-blur-md z-10">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-full bg-slate-900 text-white font-black text-sm flex items-center justify-center">
              {profile.name.charAt(0)}
            </div>
            <div>
              <h4 className="font-extrabold text-sm text-slate-900">{profile.name}</h4>
              <span className="text-[10px] text-slate-400 font-semibold">{profile.email}</span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-4 space-y-4 flex-1">
          {/* Member Card */}
          <MemberCard tier={profile.tier} currentPoints={profile.currentPoints} />

          {/* Stepper */}
          <MilestoneStepper
            currentTier={profile.tier}
            lifetimePoints={profile.lifetimePoints}
            nextTierPointsNeeded={profile.nextTierPointsNeeded}
          />

          {/* Quick Actions (Voucher, Loyalty Gift, Orders) */}
          <div className="grid grid-cols-3 gap-2">
            <button
              onClick={() => {
                onClose();
                onOpenVouchers();
              }}
              className="p-3 bg-slate-50 hover:bg-slate-100 rounded-2xl border border-slate-200 text-center transition-all"
            >
              <span className="text-base font-black text-amber-600 block">
                {profile.voucherCount}
              </span>
              <span className="text-[11px] font-bold text-slate-700">Voucher</span>
            </button>

            <button
              onClick={() => {
                onClose();
                onOpenGifts();
              }}
              className="p-3 bg-slate-50 hover:bg-slate-100 rounded-2xl border border-slate-200 text-center transition-all"
            >
              <span className="text-base font-black text-orange-600 block">
                {profile.giftCount}
              </span>
              <span className="text-[11px] font-bold text-slate-700">Gift</span>
            </button>

            <button
              onClick={() => {
                onClose();
                onOpenOrders();
              }}
              className="p-3 bg-slate-50 hover:bg-slate-100 rounded-2xl border border-slate-200 text-center transition-all"
            >
              <span className="text-base font-black text-slate-900 block">
                {profile.ordersCount}
              </span>
              <span className="text-[11px] font-bold text-slate-700">Orders</span>
            </button>
          </div>

          {/* Settings list */}
          <div className="bg-slate-50/70 rounded-2xl border border-slate-100 divide-y divide-slate-100 overflow-hidden">
            <button
              onClick={() => {
                onClose();
                onOpenProfileDetail();
              }}
              className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-100/70 transition-colors"
            >
              <div className="flex items-center gap-3">
                <UserCheck className="w-4 h-4 text-slate-500" />
                <span className="text-xs font-bold text-slate-800">Personal Infomation</span>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </button>

            <button
              onClick={() => {}}
              className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-100/70 transition-colors"
            >
              <div className="flex items-center gap-3">
                <Settings className="w-4 h-4 text-slate-500" />
                <span className="text-xs font-bold text-slate-800">Setting</span>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </button>

            <button
              onClick={() => {}}
              className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-100/70 transition-colors"
            >
              <div className="flex items-center gap-3">
                <HelpCircle className="w-4 h-4 text-slate-500" />
                <span className="text-xs font-bold text-slate-800">Help</span>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </button>

            <button
              onClick={() => {}}
              className="w-full px-4 py-3 flex items-center justify-between text-left hover:bg-slate-100/70 transition-colors"
            >
              <div className="flex items-center gap-3">
                <FileText className="w-4 h-4 text-slate-500" />
                <span className="text-xs font-bold text-slate-800">Terms & Policies</span>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </button>
          </div>
        </div>

        {/* Nút Đăng Xuất (Logout) */}
        <div className="p-4 border-t border-slate-100 bg-white">
          <button
            onClick={() => {
              onClose();
              onLogout();
            }}
            className="w-full py-3 rounded-2xl bg-red-50 hover:bg-red-100 text-red-600 font-bold text-xs flex items-center justify-center gap-2 transition-all active:scale-98"
          >
            <LogOut className="w-4 h-4" />
            <span>Logout</span>
          </button>
        </div>
      </div>
    </div>
  );
};

