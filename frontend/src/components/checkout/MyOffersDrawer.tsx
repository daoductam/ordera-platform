import React, { useState } from "react";
import { X, Ticket, Gift, Check, ArrowRight } from "lucide-react";
import type { VoucherItem, LoyaltyGiftItem } from "@/types/reward";

export interface MyOffersDrawerProps {
  isOpen: boolean;
  vouchers: VoucherItem[];
  gifts: LoyaltyGiftItem[];
  selectedVoucher: VoucherItem | null;
  selectedGift: LoyaltyGiftItem | null;
  onClose: () => void;
  onSelectVoucher: (vouch: VoucherItem | null) => void;
  onSelectGift: (gift: LoyaltyGiftItem | null) => void;
  onApplyManualCode: (code: string) => void;
}

export const MyOffersDrawer: React.FC<MyOffersDrawerProps> = ({
  isOpen,
  vouchers,
  gifts,
  selectedVoucher,
  selectedGift,
  onClose,
  onSelectVoucher,
  onSelectGift,
  onApplyManualCode,
}) => {
  const [activeTab, setActiveTab] = useState<"voucher" | "gift">("voucher");
  const [promoCodeInput, setPromoCodeInput] = useState("");

  if (!isOpen) return null;

  const handleManualApply = (e: React.FormEvent) => {
    e.preventDefault();
    if (!promoCodeInput.trim()) return;
    onApplyManualCode(promoCodeInput.trim());
    setPromoCodeInput("");
  };

  const isEmpty = vouchers.length === 0 && gifts.length === 0;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white rounded-t-3xl shadow-2xl overflow-hidden max-h-[85vh] flex flex-col animate-in slide-in-from-bottom-8 duration-300">
        {/* Header (Figma 08.02 / 08.04: Voucher & Loyalty Gifts) */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between sticky top-0 bg-white/95 backdrop-blur-md z-10">
          <div>
            <h3 className="font-extrabold text-base text-slate-900">Voucher & Loyalty Gifts</h3>
            <span className="text-[10px] text-slate-400 font-semibold">
              Select discounts or free gifts for this order
            </span>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Input Nhập mã thủ công (Figma 08.04 / 08.05) */}
        <div className="p-4 pb-2 border-b border-slate-50">
          <form onSubmit={handleManualApply} className="flex gap-2">
            <input
              type="text"
              value={promoCodeInput}
              onChange={(e) => setPromoCodeInput(e.target.value)}
              placeholder="Enter promo or gift code here"
              className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2 text-xs text-slate-900 uppercase font-bold focus:outline-hidden focus:border-amber-500"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs rounded-xl transition-all active:scale-95 shadow-xs"
            >
              Apply
            </button>
          </form>
        </div>

        {/* 2 Tabs: Voucher / Loyalty Gifts */}
        <div className="px-4 pt-2 flex items-center gap-2">
          <button
            onClick={() => setActiveTab("voucher")}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              activeTab === "voucher"
                ? "bg-amber-400 text-slate-950 font-black shadow-2xs"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200"
            }`}
          >
            <Ticket className="w-3.5 h-3.5" />
            <span>Voucher ({vouchers.length})</span>
          </button>

          <button
            onClick={() => setActiveTab("gift")}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all flex items-center justify-center gap-1.5 ${
              activeTab === "gift"
                ? "bg-amber-400 text-slate-950 font-black shadow-2xs"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200"
            }`}
          >
            <Gift className="w-3.5 h-3.5" />
            <span>Loyalty Gifts ({gifts.length})</span>
          </button>
        </div>

        {/* Body list */}
        <div className="p-4 space-y-3 overflow-y-auto flex-1">
          {isEmpty && (
            <div className="text-center py-8 space-y-2">
              <span className="text-2xl">🎟️</span>
              <h4 className="font-bold text-sm text-slate-800">My Offers · Trống</h4>
              <p className="text-xs text-slate-400">Không có ưu đãi khả dụng cho đơn hàng này.</p>
            </div>
          )}

          {/* Vouchers list */}
          {activeTab === "voucher" && (
            <div className="space-y-2.5">
              {vouchers.map((vouch) => {
                const isSelected = selectedVoucher?.id === vouch.id;
                return (
                  <div
                    key={vouch.id}
                    onClick={() => onSelectVoucher(isSelected ? null : vouch)}
                    className={`p-3.5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${
                      isSelected
                        ? "bg-amber-50/70 border-amber-400 ring-2 ring-amber-400/40 shadow-xs"
                        : "bg-white border-slate-100 hover:border-slate-200"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-amber-500 text-slate-950 font-black text-sm flex items-center justify-center shrink-0">
                        {vouch.discountType === "fixed" ? "$" : "%"}
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <h5 className="font-extrabold text-xs text-slate-900">{vouch.code}</h5>
                          <span className="px-1.5 py-0.2 rounded bg-amber-100 text-amber-900 text-[9px] font-black">
                            {vouch.discountValue} {vouch.discountType === "fixed" ? "NOK" : "%"}
                          </span>
                        </div>
                        <p className="text-[11px] text-slate-500 mt-0.5">{vouch.description}</p>
                      </div>
                    </div>

                    <div
                      className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border ${
                        isSelected
                          ? "bg-amber-400 border-amber-500 text-slate-950 shadow-2xs"
                          : "border-slate-300 bg-white"
                      }`}
                    >
                      {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Gifts list */}
          {activeTab === "gift" && (
            <div className="space-y-2.5">
              {gifts.map((gift) => {
                const isSelected = selectedGift?.id === gift.id;
                return (
                  <div
                    key={gift.id}
                    onClick={() => onSelectGift(isSelected ? null : gift)}
                    className={`p-3.5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${
                      isSelected
                        ? "bg-orange-50/70 border-orange-400 ring-2 ring-orange-400/40 shadow-xs"
                        : "bg-white border-slate-100 hover:border-slate-200"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-xl bg-slate-100 overflow-hidden shrink-0 border border-slate-100">
                        <img
                          src={gift.image}
                          alt={gift.title}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div>
                        <span className="text-[9px] font-black uppercase text-orange-600 bg-orange-100 px-1.5 py-0.2 rounded">
                          {gift.tag}
                        </span>
                        <h5 className="font-extrabold text-xs text-slate-900 mt-0.5">
                          {gift.title}
                        </h5>
                        <p className="text-[11px] text-slate-500 mt-0.5">{gift.description}</p>
                      </div>
                    </div>

                    <div
                      className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 border ${
                        isSelected
                          ? "bg-orange-500 border-orange-600 text-white shadow-2xs"
                          : "border-slate-300 bg-white"
                      }`}
                    >
                      {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer Apply Button */}
        <div className="p-4 border-t border-slate-100 bg-slate-50">
          <button
            onClick={onClose}
            className="w-full py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs transition-all active:scale-98 shadow-sm flex items-center justify-center gap-1.5"
          >
            <span>Done</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

