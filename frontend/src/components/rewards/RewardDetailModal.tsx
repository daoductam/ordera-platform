import React, { useState } from "react";
import { X, CheckCircle2 } from "lucide-react";
import type { VoucherItem, LoyaltyGiftItem } from "@/types/reward";

export interface RewardDetailModalProps {
  isOpen: boolean;
  reward: VoucherItem | LoyaltyGiftItem | null;
  onClose: () => void;
  onApply: (item: VoucherItem | LoyaltyGiftItem) => void;
}

export const RewardDetailModal: React.FC<RewardDetailModalProps> = ({
  isOpen,
  reward,
  onClose,
  onApply,
}) => {
  const [isApplied, setIsApplied] = useState(false);

  if (!isOpen || !reward) return null;

  const isVoucher = "discountValue" in reward;

  const handleUseNow = () => {
    setIsApplied(true);
    setTimeout(() => {
      onApply(reward);
      setIsApplied(false);
      onClose();
    }, 900);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-slate-900">
            {isVoucher ? "Voucher Details" : "Loyalty Gift Details"}
          </h3>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <div className="p-5 space-y-4">
          <div className="text-center space-y-2">
            <div className="w-14 h-14 rounded-3xl bg-amber-500 text-slate-950 font-black text-2xl flex items-center justify-center mx-auto shadow-md">
              {isVoucher ? "$" : "🎁"}
            </div>
            <h4 className="font-black text-base text-slate-900">{reward.title}</h4>
            <p className="text-xs text-slate-600">{reward.description}</p>
          </div>

          <div className="bg-slate-50 rounded-2xl p-3.5 space-y-1.5 text-xs text-slate-600 border border-slate-100">
            {isVoucher && (
              <>
                <div className="flex justify-between">
                  <span className="text-slate-400">Giá trị giảm:</span>
                  <span className="font-bold text-slate-900">
                    {reward.discountValue} {reward.discountType === "fixed" ? "NOK" : "%"}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Đơn hàng tối thiểu:</span>
                  <span className="font-bold text-slate-900">{reward.minOrderValue} NOK</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Hạn sử dụng:</span>
                  <span className="font-bold text-slate-900">{reward.expiryDate}</span>
                </div>
              </>
            )}

            {!isVoucher && (
              <>
                <div className="flex justify-between">
                  <span className="text-slate-400">Phân loại quà:</span>
                  <span className="font-bold text-slate-900">{reward.tag}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Số lượng còn lại:</span>
                  <span className="font-bold text-slate-900">{reward.quantityLeft} suất</span>
                </div>
              </>
            )}
          </div>

          {/* Action button: Use now or Applied confirmation (Figma 07.07 & 07.08) */}
          <div className="pt-2">
            <button
              onClick={handleUseNow}
              className={`w-full py-3 rounded-2xl font-black text-xs transition-all flex items-center justify-center gap-1.5 shadow-xs active:scale-98 ${
                isApplied
                  ? "bg-emerald-600 text-white"
                  : "bg-amber-400 hover:bg-amber-500 text-slate-950"
              }`}
            >
              {isApplied ? (
                <>
                  <CheckCircle2 className="w-4 h-4 stroke-[2.5]" />
                  <span>Áp dụng thành công!</span>
                </>
              ) : (
                <span>Use now</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
