import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft } from "lucide-react";
import type { VoucherItem, LoyaltyGiftItem } from "@/types/reward";
import {
  MOCK_MY_VOUCHERS,
  MOCK_MY_GIFTS,
} from "@/mock/rewardMock";
import { MyRewardsView } from "@/components/rewards/MyRewardsView";
import { GetMoreRewardsView } from "@/components/rewards/GetMoreRewardsView";
import { EmptyRewardsView } from "@/components/rewards/EmptyRewardsView";

export const RewardsPage: React.FC = () => {
  const navigate = useNavigate();

  // Tab chính: My Rewards (07.03/07.07/07.08) hoặc Get More Rewards (07.04)
  const [activeTab, setActiveTab] = useState<"myRewards" | "getMore">("myRewards");

  // Sub-filter: "voucher" hoặc "gift" (Pills chuẩn Figma)
  const [subFilter, setSubFilter] = useState<"voucher" | "gift">("voucher");

  // Danh sách voucher sở hữu (Chuẩn Figma 07.03)
  const [myVouchers, setMyVouchers] = useState<VoucherItem[]>(MOCK_MY_VOUCHERS);
  const [myGifts, setMyGifts] = useState<LoyaltyGiftItem[]>(MOCK_MY_GIFTS);

  // Điểm hiện tại của David Nguyen (120 điểm - Hạng Silver)
  const [currentPoints, setCurrentPoints] = useState(120);

  // Voucher đang được áp dụng (Trạng thái Figma 07.08)
  const [appliedVoucherId, setAppliedVoucherId] = useState<string | null>(null);

  // Toggle mô phỏng trạng thái trống (Figma 07.05 & 07.06)
  const [simulateEmpty, setSimulateEmpty] = useState(false);

  // Áp dụng voucher -> Chuyển sang 07.08
  const handleApplyVoucher = (voucher: VoucherItem) => {
    setAppliedVoucherId(voucher.id);
  };

  // Huỷ áp dụng voucher -> Quay về 07.07
  const handleRemoveVoucher = () => {
    setAppliedVoucherId(null);
  };

  // Đổi voucher bằng điểm ở tab Get More Rewards (Figma 07.04)
  const handleRedeemVoucher = (item: {
    id: string;
    title: string;
    discountValue: number;
    discountType: "fixed" | "percent";
    pointsCost: number;
  }) => {
    if (currentPoints >= item.pointsCost) {
      setCurrentPoints((prev) => prev - item.pointsCost);
      const newVouch: VoucherItem = {
        id: `redeemed-${Date.now()}`,
        code: `SAVE${item.discountValue}`,
        title: item.title,
        description:
          item.discountType === "fixed"
            ? `Giảm ${item.discountValue} NOK giá trị cho Order của bạn`
            : `Giảm ${item.discountValue}% giá trị cho đơn hàng của bạn`,
        discountType: item.discountType,
        discountValue: item.discountValue,
        minOrderValue: item.discountType === "fixed" ? 250 : 0,
        expiryDate: "3 dec 2025",
        isRedeemed: false,
      };
      setMyVouchers((prev) => [newVouch, ...prev]);
      alert(`Đổi thành công: ${item.title} (-${item.pointsCost} Points)!`);
      setActiveTab("myRewards");
    }
  };

  // Đổi quà tặng bằng điểm ở tab Get More Rewards (Figma 07.04)
  const handleRedeemGift = (item: {
    id: string;
    title: string;
    pointsCost: number;
  }) => {
    if (currentPoints >= item.pointsCost) {
      setCurrentPoints((prev) => prev - item.pointsCost);
      const newGift: LoyaltyGiftItem = {
        id: `gift-${Date.now()}`,
        title: item.title,
        description: "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
        tag: "Gift",
        pointsCost: item.pointsCost,
        quantityLeft: 9,
        image:
          "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=160&auto=format&fit=crop&q=80",
      };
      setMyGifts((prev) => [newGift, ...prev]);
      alert(`Đổi thành công: ${item.title} (-${item.pointsCost} Points)!`);
      setActiveTab("myRewards");
    }
  };

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center">
      <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl flex flex-col">
        {/* HEADER CHUẨN FIGMA 07.03 - 07.08: Chevron Trái | Tiêu đề Giữa | Avatar Phải */}
        <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100">
          <button
            onClick={() => navigate(-1)}
            aria-label="Back"
            className="w-8 h-8 -ml-1 flex items-center justify-center text-slate-800 hover:text-slate-950 active:scale-95 transition-all cursor-pointer"
          >
            <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
          </button>

          <h1 className="font-bold text-sm text-slate-900 tracking-tight select-none">
            Voucher & Loyalty Gifts
          </h1>

          {/* Avatar David Nguyen có viền tròn gradient hồng/tím chuẩn Figma 07.03 */}
          <button
            onClick={() => navigate("/account")}
            aria-label="Go to Account"
            className="w-7 h-7 rounded-full p-[1.5px] bg-gradient-to-tr from-pink-500 to-rose-400 overflow-hidden active:scale-95 transition-all cursor-pointer"
          >
            <img
              src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80&auto=format&fit=crop&q=80"
              alt="David Nguyen"
              className="w-full h-full rounded-full object-cover"
            />
          </button>
        </header>

        {/* 2 MAIN TABS: My Rewards | Get More Rewards (Chuẩn Figma 07.03 - 07.06) */}
        <div className="flex border-b border-slate-100 bg-white px-4 relative select-none">
          <button
            onClick={() => setActiveTab("myRewards")}
            className={`flex-1 py-3 text-xs font-bold text-center transition-all cursor-pointer relative ${
              activeTab === "myRewards"
                ? "text-[#E5A93C]"
                : "text-slate-500 hover:text-slate-700"
            }`}
          >
            My Rewards
            {activeTab === "myRewards" && (
              <span className="absolute bottom-0 left-6 right-6 h-0.5 bg-[#E5A93C] rounded-full" />
            )}
          </button>

          <button
            onClick={() => setActiveTab("getMore")}
            className={`flex-1 py-3 text-xs font-bold text-center transition-all cursor-pointer relative ${
              activeTab === "getMore"
                ? "text-[#E5A93C]"
                : "text-slate-500 hover:text-slate-700"
            }`}
          >
            Get More Rewards
            {activeTab === "getMore" && (
              <span className="absolute bottom-0 left-6 right-6 h-0.5 bg-[#E5A93C] rounded-full" />
            )}
          </button>

          {/* Nút Demo trạng thái Trống (07.05 & 07.06) */}
          <button
            onClick={() => setSimulateEmpty((prev) => !prev)}
            className="absolute right-2 top-2.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 font-medium transition-all"
            title="Bấm để xem màn hình Trống 07.05 & 07.06"
          >
            {simulateEmpty ? "Dữ liệu" : "Trống"}
          </button>
        </div>

        {/* NỘI DUNG CHÍNH */}
        <div className="p-4 flex-1">
          {/* 1. TAB MY REWARDS */}
          {activeTab === "myRewards" &&
            (simulateEmpty ? (
              // Trạng thái trống của My Rewards (Figma 07.05)
              <EmptyRewardsView
                type="myRewards"
                onAction={() => setActiveTab("getMore")}
              />
            ) : (
              // Danh sách voucher & quà của My Rewards (Figma 07.03 / 07.07 / 07.08)
              <MyRewardsView
                vouchers={myVouchers}
                gifts={myGifts}
                appliedVoucherId={appliedVoucherId}
                onApplyVoucher={handleApplyVoucher}
                onRemoveVoucher={handleRemoveVoucher}
                onGoToShop={() => navigate("/")}
                subFilter={subFilter}
                onSelectSubFilter={(filter) => {
                  setSubFilter(filter);
                  const el = document.getElementById(
                    filter === "voucher" ? "voucher-section" : "gift-section"
                  );
                  if (el) el.scrollIntoView({ behavior: "smooth" });
                }}
              />
            ))}

          {/* 2. TAB GET MORE REWARDS */}
          {activeTab === "getMore" &&
            (simulateEmpty ? (
              // Trạng thái trống của Đổi thưởng (Figma 07.06)
              <EmptyRewardsView
                type="getMore"
                onAction={() => navigate("/")}
              />
            ) : (
              // Danh sách đổi điểm (Figma 07.04)
              <GetMoreRewardsView
                currentPoints={currentPoints}
                subFilter={subFilter}
                onSelectSubFilter={(filter) => {
                  setSubFilter(filter);
                  const el = document.getElementById(
                    filter === "voucher"
                      ? "redeem-voucher-section"
                      : "redeem-gift-section"
                  );
                  if (el) el.scrollIntoView({ behavior: "smooth" });
                }}
                onRedeemVoucher={handleRedeemVoucher}
                onRedeemGift={handleRedeemGift}
              />
            ))}
        </div>
      </div>
    </div>
  );
};
