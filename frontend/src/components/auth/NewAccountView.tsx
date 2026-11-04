import React from "react";
import { ChevronLeft, Bell, User, Ticket, Gift, Package, ChevronRight, Settings, HelpCircle, FileText, UserCheck } from "lucide-react";
import type { AuthUser } from "@/types/auth";
import { MemberCard } from "@/components/profile/MemberCard";
import { MilestoneStepper } from "@/components/profile/MilestoneStepper";

export interface NewAccountViewProps {
  user: AuthUser;
  onBack: () => void;
  onGoToVouchers?: () => void;
  onGoToOrders?: () => void;
}

export const NewAccountView: React.FC<NewAccountViewProps> = ({
  user,
  onBack,
  onGoToVouchers,
  onGoToOrders,
}) => {
  const menuItems = [
    { label: "Personal Information", icon: UserCheck },
    { label: "Setting", icon: Settings },
    { label: "Help", icon: HelpCircle },
    { label: "Terms & Policies", icon: FileText },
  ];

  return (
    <div className="bg-slate-50 min-h-screen pb-20">
      {/* 1. Header (Figma 03.10: < ABY TRAN + Bell + Avatar) */}
      <div className="bg-white px-4 py-3 border-b border-slate-100 flex items-center justify-between sticky top-0 z-20 shadow-2xs">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 text-slate-800 font-extrabold text-sm hover:text-slate-900"
        >
          <ChevronLeft className="w-5 h-5" />
          <span>{user.name}</span>
        </button>

        <div className="flex items-center gap-2.5">
          <button className="w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-600 transition-colors">
            <Bell className="w-4 h-4" />
          </button>
          <div className="w-9 h-9 rounded-full bg-amber-400 text-slate-950 font-black text-xs flex items-center justify-center shadow-xs">
            <User className="w-5 h-5" />
          </div>
        </div>
      </div>

      <div className="p-4 space-y-4">
        {/* 2. Vibrant Emerald Green Member Card (Figma 03.10) */}
        <MemberCard tier="New" currentPoints={user.currentPoints} />

        {/* 3. Milestone Stepper */}
        <MilestoneStepper
          currentTier="New"
          lifetimePoints={user.lifetimePoints}
          nextTierPointsNeeded={200}
        />

        {/* 3. Quick Action Cards (Voucher, Loyalty Gift, Orders) */}
        <div className="grid grid-cols-3 gap-2.5">
          <button
            onClick={onGoToVouchers}
            className="bg-white rounded-2xl p-3 border border-slate-100 shadow-2xs hover:shadow-xs transition-all flex flex-col items-center justify-center gap-1.5 active:scale-95 text-slate-800"
          >
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
              <Ticket className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold">Voucher</span>
          </button>

          <button
            onClick={onGoToVouchers}
            className="bg-white rounded-2xl p-3 border border-slate-100 shadow-2xs hover:shadow-xs transition-all flex flex-col items-center justify-center gap-1.5 active:scale-95 text-slate-800"
          >
            <div className="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center">
              <Gift className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold">Loyalty Gift</span>
          </button>

          <button
            onClick={onGoToOrders}
            className="bg-white rounded-2xl p-3 border border-slate-100 shadow-2xs hover:shadow-xs transition-all flex flex-col items-center justify-center gap-1.5 active:scale-95 text-slate-800"
          >
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Package className="w-5 h-5" />
            </div>
            <span className="text-xs font-bold">Orders</span>
          </button>
        </div>

        {/* 4. Menu Items List */}
        <div className="bg-white rounded-2xl p-2 border border-slate-100 shadow-2xs divide-y divide-slate-100 text-xs">
          {menuItems.map((item, idx) => {
            const Icon = item.icon;
            return (
              <button
                key={idx}
                className="w-full py-3.5 px-3 flex items-center justify-between text-slate-700 hover:text-slate-900 group transition-colors"
              >
                <span className="flex items-center gap-3 font-semibold">
                  <Icon className="w-4 h-4 text-slate-400 group-hover:text-amber-600 transition-colors" />
                  {item.label}
                </span>
                <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

