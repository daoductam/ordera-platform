import React from "react";
import { Check } from "lucide-react";
import type { VoucherItem, LoyaltyGiftItem } from "@/types/reward";
import { VoucherTicketIcon } from "./VoucherTicketIcon";

export interface MyRewardsViewProps {
  vouchers: VoucherItem[];
  gifts: LoyaltyGiftItem[];
  appliedVoucherId?: string | null;
  onApplyVoucher: (voucher: VoucherItem) => void;
  onRemoveVoucher: () => void;
  onGoToShop: () => void;
  subFilter: "voucher" | "gift";
  onSelectSubFilter: (f: "voucher" | "gift") => void;
}

export const MyRewardsView: React.FC<MyRewardsViewProps> = ({
  vouchers,
  gifts,
  appliedVoucherId,
  onApplyVoucher,
  onRemoveVoucher,
  onGoToShop,
  subFilter,
  onSelectSubFilter,
}) => {
  return (
    <div className="space-y-4 text-slate-900 font-sans pb-36">
      {/* 1. SUB-FILTERS: Voucher • Loyalty Gifts (Pills chuẩn Figma 07.03) */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => onSelectSubFilter("voucher")}
          className={`px-4 py-1.5 rounded-full text-xs transition-all cursor-pointer ${
            subFilter === "voucher"
              ? "bg-[#F5B843] text-slate-950 font-bold shadow-2xs"
              : "bg-white border border-slate-200 text-slate-500 font-medium hover:border-slate-300"
          }`}
        >
          Voucher
        </button>
        <button
          onClick={() => onSelectSubFilter("gift")}
          className={`px-4 py-1.5 rounded-full text-xs transition-all cursor-pointer ${
            subFilter === "gift"
              ? "bg-[#F5B843] text-slate-950 font-bold shadow-2xs"
              : "bg-white border border-slate-200 text-slate-500 font-medium hover:border-slate-300"
          }`}
        >
          Loyalty Gifts
        </button>
      </div>

      {/* 2. SECTION VOUCHER (Hiển thị khi subFilter === "voucher" hoặc cả 2 theo chuẩn Figma 07.03 & 07.07 & 07.08) */}
      {(subFilter === "voucher" || subFilter === "gift") && (
        <section id="voucher-section" className="space-y-3">
          <h2 className="text-sm font-extrabold text-slate-900 tracking-tight">
            Voucher
          </h2>

          <div className="space-y-3">
            {vouchers.map((vouch, idx) => {
              const isApplied = appliedVoucherId === vouch.id;
              const isFixed = vouch.discountType === "fixed";
              const displayValue = isFixed
                ? `${vouch.discountValue} NOK`
                : `-${vouch.discountValue} %`;

              return (
                <div
                  key={vouch.id || idx}
                  className="bg-white rounded-2xl p-4 border border-slate-100 shadow-[0_2px_8px_rgba(0,0,0,0.03)] space-y-2.5 transition-all"
                >
                  <div className="flex items-start gap-3.5">
                    {/* Icon Ticket vàng có $ hoặc % chuẩn Figma */}
                    <VoucherTicketIcon type={isFixed ? "fixed" : "percent"} />

                    {/* Nội dung thông tin voucher */}
                    <div className="flex-1 min-w-0 space-y-1">
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-xs text-slate-900">
                          {vouch.title || "Voucher Name"}
                        </span>
                        <span className="font-extrabold text-xs text-slate-900">
                          {displayValue}
                        </span>
                      </div>

                      <p className="text-[11px] text-slate-500 font-medium leading-relaxed">
                        {vouch.description ||
                          (isFixed
                            ? `Giảm ${vouch.discountValue} NOK giá trị cho Order của bạn`
                            : `Giảm ${vouch.discountValue}% giá trị cho đơn hàng của bạn`)}
                      </p>

                      <div className="text-[10px] text-slate-400 font-medium">
                        Minimum Order Value: {vouch.minOrderValue || 0} NOK
                      </div>

                      <div className="text-[10px] text-slate-400 font-medium">
                        Expired date: {vouch.expiryDate || "3 dec 2025"}
                      </div>
                    </div>
                  </div>

                  {/* Hành động dưới thẻ: Use now hoặc In Use & Remove button (Figma 07.08) */}
                  <div className="pt-1 flex items-center justify-end">
                    {isApplied ? (
                      <div className="w-full flex items-center justify-between">
                        {/* Huy hiệu In Use màu xanh lá chuẩn Figma 07.08 */}
                        <span className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-[#E8F8F0] text-[#10B981] text-[11px] font-bold">
                          <span className="w-1.5 h-1.5 rounded-full bg-[#10B981]" />
                          <span>In Use</span>
                        </span>

                        {/* Nút Remove from the Cart chuẩn Figma 07.08 */}
                        <button
                          onClick={onRemoveVoucher}
                          className="px-3 py-1 rounded-lg border border-slate-300 bg-white text-slate-700 font-medium text-xs hover:bg-slate-50 active:scale-95 transition-all shadow-2xs cursor-pointer"
                        >
                          Remove from the Cart
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => onApplyVoucher(vouch)}
                        className="text-[#10B981] font-bold text-xs hover:underline active:scale-95 transition-all cursor-pointer"
                      >
                        Use now
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* 3. SECTION LOYALTY GIFTS (Hiển thị ngay dưới Voucher chuẩn Figma 07.03 & 07.07 & 07.08) */}
      {(subFilter === "voucher" || subFilter === "gift") && (
        <section id="gift-section" className="space-y-3 pt-2">
          <h2 className="text-sm font-extrabold text-slate-900 tracking-tight">
            Loyalty Gifts
          </h2>

          <div className="space-y-3">
            {gifts.map((gift, idx) => (
              <div
                key={gift.id || idx}
                className="bg-white rounded-2xl p-4 border border-slate-100 shadow-[0_2px_8px_rgba(0,0,0,0.03)] space-y-2.5"
              >
                <div className="flex items-start gap-3.5">
                  {/* Ảnh ly giữ nhiệt tumbler màu hồng chuẩn Figma 07.03 */}
                  <div className="w-14 h-16 rounded-xl bg-slate-50 flex items-center justify-center shrink-0 overflow-hidden p-1 border border-slate-100">
                    <img
                      src={
                        gift.image ||
                        "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=160&auto=format&fit=crop&q=80"
                      }
                      alt={gift.title}
                      className="w-full h-full object-contain"
                    />
                  </div>

                  {/* Thông tin quà tặng */}
                  <div className="flex-1 min-w-0 space-y-1">
                    <h3 className="font-bold text-xs text-slate-900">
                      {gift.title || "Gift Name"}
                    </h3>

                    <p className="text-[11px] text-slate-500 font-medium leading-relaxed">
                      {gift.description ||
                        "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào"}
                    </p>

                    <div className="text-[10px] text-slate-400 font-medium">
                      Quantity left: 10 Pieces
                    </div>
                  </div>
                </div>

                {/* Nút Get now màu xanh lá chuẩn Figma 07.03 */}
                <div className="pt-1 flex justify-end">
                  <button
                    onClick={() => {
                      alert(`Đã nhận quà tặng: ${gift.title}!`);
                    }}
                    className="text-[#10B981] font-bold text-xs hover:underline active:scale-95 transition-all cursor-pointer"
                  >
                    Get now
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* 4. THANH THÔNG BÁO THÀNH CÔNG VÀ NÚT GO TO SHOP (Figma 07.08) */}
      {appliedVoucherId && (
        <div className="fixed bottom-16 left-0 right-0 z-30 p-4 max-w-md mx-auto space-y-2">
          {/* Thông báo xanh lá: Voucher added successfully */}
          <div className="flex items-center justify-center gap-1.5 text-xs font-semibold text-[#10B981] bg-[#E8F8F0] py-2 rounded-xl border border-emerald-100 shadow-2xs">
            <Check className="w-4 h-4 stroke-[3]" />
            <span>Voucher added successfully</span>
          </div>

          {/* Nút vàng cam Go to Shop */}
          <button
            onClick={onGoToShop}
            className="w-full py-3.5 rounded-2xl bg-[#F5B843] hover:bg-[#DEA100] active:scale-[0.99] text-slate-950 font-bold text-sm text-center shadow-xs transition-all cursor-pointer"
          >
            Go to Shop
          </button>
        </div>
      )}
    </div>
  );
};
