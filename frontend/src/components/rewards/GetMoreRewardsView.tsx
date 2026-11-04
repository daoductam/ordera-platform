import React from "react";
import { VoucherTicketIcon } from "./VoucherTicketIcon";

export interface GetMoreRewardsViewProps {
  currentPoints: number;
  subFilter: "voucher" | "gift";
  onSelectSubFilter: (f: "voucher" | "gift") => void;
  onRedeemVoucher: (item: {
    id: string;
    title: string;
    discountValue: number;
    discountType: "fixed" | "percent";
    pointsCost: number;
  }) => void;
  onRedeemGift: (item: {
    id: string;
    title: string;
    pointsCost: number;
  }) => void;
}

export const GetMoreRewardsView: React.FC<GetMoreRewardsViewProps> = ({
  currentPoints,
  subFilter,
  onSelectSubFilter,
  onRedeemVoucher,
  onRedeemGift,
}) => {
  const redeemableVouchers = [
    {
      id: "rd-1",
      title: "Voucher Name",
      discountValue: 100,
      discountType: "fixed" as const,
      pointsCost: 100,
      description: "Giảm 100 NOK giá trị cho Order của bạn",
      minOrderValue: 250,
      expiryDate: "3 dec 2025",
    },
    {
      id: "rd-2",
      title: "Voucher Name",
      discountValue: 30,
      discountType: "percent" as const,
      pointsCost: 1000,
      description: "Giảm 30% giá trị cho đơn hàng của bạn",
      minOrderValue: 0,
      expiryDate: "3 dec 2025",
    },
  ];

  const redeemableGifts = [
    {
      id: "gift-1",
      title: "Gift Name",
      pointsCost: 100,
      description: "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
      quantityLeft: 10,
      imageUrl:
        "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=160&auto=format&fit=crop&q=80",
    },
  ];

  return (
    <div className="space-y-4 text-slate-900 font-sans pb-36">
      {/* 1. SUB-FILTERS: Voucher • Loyalty Gifts (Pills chuẩn Figma 07.04) */}
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

      {/* 2. SECTION VOUCHER (Chuẩn Figma 07.04) */}
      {(subFilter === "voucher" || subFilter === "gift") && (
        <section id="redeem-voucher-section" className="space-y-3">
          <h2 className="text-sm font-extrabold text-slate-900 tracking-tight">
            Voucher
          </h2>

          <div className="space-y-3">
            {redeemableVouchers.map((vouch) => {
              const canAfford = currentPoints >= vouch.pointsCost;
              const isFixed = vouch.discountType === "fixed";
              const displayValue = isFixed
                ? `${vouch.discountValue} NOK`
                : `-${vouch.discountValue} %`;

              return (
                <div
                  key={vouch.id}
                  className="bg-white rounded-2xl p-4 border border-slate-100 shadow-[0_2px_8px_rgba(0,0,0,0.03)] space-y-3"
                >
                  <div className="flex items-start gap-3.5">
                    {/* Icon Ticket vàng có $ hoặc % chuẩn Figma */}
                    <VoucherTicketIcon type={isFixed ? "fixed" : "percent"} />

                    {/* Thông tin voucher */}
                    <div className="flex-1 min-w-0 space-y-1">
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-xs text-slate-900">
                          {vouch.title}
                        </span>
                        <span className="font-extrabold text-xs text-slate-900">
                          {displayValue}
                        </span>
                      </div>

                      <p className="text-[11px] text-slate-500 font-medium leading-relaxed">
                        {vouch.description}
                      </p>

                      <div className="text-[10px] text-slate-400 font-medium">
                        Minimum Order Value: {vouch.minOrderValue} NOK
                      </div>

                      <div className="text-[10px] text-slate-400 font-medium">
                        Expired date: {vouch.expiryDate}
                      </div>
                    </div>
                  </div>

                  {/* Hành động đổi điểm: 100 Points màu đỏ + Nút Redeem chuẩn Figma 07.04 */}
                  <div className="pt-1 flex items-center justify-between">
                    <span className="text-xs font-bold text-red-500">
                      {vouch.pointsCost} Points
                    </span>

                    <button
                      onClick={() => onRedeemVoucher(vouch)}
                      disabled={!canAfford}
                      className={`px-8 py-2 rounded-lg text-xs font-bold transition-all shadow-2xs ${
                        canAfford
                          ? "bg-[#F5B843] hover:bg-[#DEA100] text-slate-900 active:scale-95 cursor-pointer shadow-xs"
                          : "bg-[#D1D5DB] text-slate-400 cursor-not-allowed"
                      }`}
                    >
                      Redeem
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* 3. SECTION LOYALTY GIFTS (Chuẩn Figma 07.04) */}
      {(subFilter === "voucher" || subFilter === "gift") && (
        <section id="redeem-gift-section" className="space-y-3 pt-2">
          <h2 className="text-sm font-extrabold text-slate-900 tracking-tight">
            Loyalty Gifts
          </h2>

          <div className="space-y-3">
            {redeemableGifts.map((gift) => {
              const canAfford = currentPoints >= gift.pointsCost;

              return (
                <div
                  key={gift.id}
                  className="bg-white rounded-2xl p-4 border border-slate-100 shadow-[0_2px_8px_rgba(0,0,0,0.03)] space-y-3"
                >
                  <div className="flex items-start gap-3.5">
                    {/* Ảnh cốc tumbler */}
                    <div className="w-14 h-16 rounded-xl bg-slate-50 flex items-center justify-center shrink-0 overflow-hidden p-1 border border-slate-100">
                      <img
                        src={gift.imageUrl}
                        alt={gift.title}
                        className="w-full h-full object-contain"
                      />
                    </div>

                    {/* Thông tin quà */}
                    <div className="flex-1 min-w-0 space-y-1">
                      <h3 className="font-bold text-xs text-slate-900">
                        {gift.title}
                      </h3>

                      <p className="text-[11px] text-slate-500 font-medium leading-relaxed">
                        {gift.description}
                      </p>

                      <div className="text-[10px] text-slate-400 font-medium">
                        Quantity left: {gift.quantityLeft} Pieces
                      </div>
                    </div>
                  </div>

                  {/* Hành động đổi quà: 100 Points màu đỏ + Nút Redeem chuẩn Figma 07.04 */}
                  <div className="pt-1 flex items-center justify-between">
                    <span className="text-xs font-bold text-red-500">
                      {gift.pointsCost} Points
                    </span>

                    <button
                      onClick={() => onRedeemGift(gift)}
                      disabled={!canAfford}
                      className={`px-8 py-2 rounded-lg text-xs font-bold transition-all shadow-2xs ${
                        canAfford
                          ? "bg-[#F5B843] hover:bg-[#DEA100] text-slate-900 active:scale-95 cursor-pointer shadow-xs"
                          : "bg-[#D1D5DB] text-slate-400 cursor-not-allowed"
                      }`}
                    >
                      Redeem
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      )}
    </div>
  );
};
