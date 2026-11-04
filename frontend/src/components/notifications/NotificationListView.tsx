import React, { useState } from "react";
import { ChevronLeft, Bell, Gift, Ticket, MessageSquare, Newspaper, ExternalLink } from "lucide-react";
import type { NotificationCategory, SystemNotification } from "@/types/notification";
import { MOCK_NOTIFICATIONS } from "@/mock/notificationMock";

export interface NotificationListViewProps {
  onBack: () => void;
  onSelectNotification: (item: SystemNotification) => void;
  onOpenSupportModal: () => void;
}

export const NotificationListView: React.FC<NotificationListViewProps> = ({
  onBack,
  onSelectNotification,
  onOpenSupportModal,
}) => {
  const [activeCategory, setActiveCategory] = useState<NotificationCategory>("Messages");

  const categories: { key: NotificationCategory; label: string; icon: any }[] = [
    { key: "Messages", label: "Messages", icon: MessageSquare },
    { key: "News", label: "News", icon: Newspaper },
    { key: "Voucher", label: "Voucher", icon: Ticket },
    { key: "Loyalty Gifts", label: "Loyalty Gifts", icon: Gift },
  ];

  const filtered = MOCK_NOTIFICATIONS.filter((n) => n.category === activeCategory);

  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-24">
      {/* 1. TOP HEADER (Figma 05.02: < Content for you) */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 text-slate-900 hover:text-slate-700 active:scale-95 transition-all"
        >
          <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
          <span className="font-black text-sm tracking-tight">Content for you</span>
        </button>

        <span className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600">
          <Bell className="w-4 h-4" />
        </span>
      </header>

      {/* 2. 4 CATEGORY TABS (Figma 05.02: Messages • News • Voucher • Loyalty Gifts) */}
      <div className="px-4 pt-3 pb-2 border-b border-slate-100 flex items-center gap-2 overflow-x-auto no-scrollbar">
        {categories.map((cat) => {
          const Icon = cat.icon;
          const isSelected = activeCategory === cat.key;
          return (
            <button
              key={cat.key}
              onClick={() => setActiveCategory(cat.key)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold transition-all shrink-0 ${
                isSelected
                  ? "bg-slate-900 text-white shadow-xs font-black"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{cat.label}</span>
            </button>
          );
        })}
      </div>

      {/* 3. DANH SÁCH THÔNG BÁO */}
      <div className="p-4 space-y-3">
        {/* Banner hỗ trợ điểm luôn nổi bật trong Messages */}
        {activeCategory === "Messages" && (
          <div className="bg-[#FFFBEB] border border-[#FDE68A] rounded-2xl p-4 shadow-2xs space-y-2">
            <span className="text-[10px] font-black uppercase text-amber-700 bg-amber-100 px-2 py-0.5 rounded-md">
              Hỗ trợ điểm
            </span>
            <h4 className="font-extrabold text-sm text-amber-950">Missing Your Points?</h4>
            <p className="text-xs text-amber-900/80 leading-relaxed">
              We've upgraded our system to improve your experience. If you don't see your loyalty
              points, please contact us or submit a request — we'll help you retrieve them!
            </p>
            <div className="pt-1 flex justify-end">
              <button
                onClick={onOpenSupportModal}
                className="px-3.5 py-1.5 bg-[#FBBF24] hover:bg-[#F59E0B] text-slate-950 font-black text-xs rounded-full shadow-2xs transition-all active:scale-95 flex items-center gap-1"
              >
                <span>Learn more</span>
                <ExternalLink className="w-3 h-3 stroke-[2.5]" />
              </button>
            </div>
          </div>
        )}

        {/* Các thẻ thông báo khác */}
        {filtered.map((item) => (
          <div
            key={item.id}
            onClick={() => onSelectNotification(item)}
            className="bg-white rounded-2xl p-4 border border-slate-100 shadow-2xs hover:border-slate-200 transition-all cursor-pointer space-y-1.5"
          >
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-slate-400">{item.date}</span>
              {!item.isRead && (
                <span className="w-2 h-2 rounded-full bg-amber-500 shadow-2xs" />
              )}
            </div>
            <h4 className="font-black text-xs text-slate-900">{item.title}</h4>
            <p className="text-[11px] text-slate-500 leading-snug line-clamp-2">
              {item.summary}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

