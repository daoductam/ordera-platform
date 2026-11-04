import React, { useState } from "react";
import {
  ArrowLeft,
  Store,
  UtensilsCrossed,
  Zap,
  Calendar,
  Tag,
  ChevronRight,
  X,
} from "lucide-react";
import type { CheckoutDetails, TipOption } from "@/types/checkout";
import { PromoCodeModal } from "./PromoCodeModal";
import { MyOffersDrawer } from "./MyOffersDrawer";
import type { VoucherItem } from "@/types/reward";
import { MOCK_MY_VOUCHERS, MOCK_MY_GIFTS } from "@/mock/rewardMock";

export interface CheckoutViewProps {
  initialDetails: CheckoutDetails;
  onBack: () => void;
  onPaySuccess: (finalDetails: CheckoutDetails) => void;
}

export const CheckoutView: React.FC<CheckoutViewProps> = ({
  initialDetails,
  onBack,
  onPaySuccess,
}) => {
  const [pickupMethod, setPickupMethod] = useState<"counter" | "table">("counter");
  const [pickupTime, setPickupTime] = useState<"asap" | "schedule">("asap");
  const [selectedTip, setSelectedTip] = useState<TipOption>("none");
  const [name, setName] = useState("David Nguyen");
  const [phone, setPhone] = useState("97362748");
  const [agreeTerms, setAgreeTerms] = useState(false);

  // Modals for promo code and offers
  const [isOffersDrawerOpen, setIsOffersDrawerOpen] = useState(false);
  const [isPromoModalOpen, setIsPromoModalOpen] = useState(false);
  const [appliedVoucher, setAppliedVoucher] = useState<VoucherItem | null>(null);

  const baseSubtotal = initialDetails.subtotal || 267;

  // Tính tiền tip
  const getTipAmount = () => {
    switch (selectedTip) {
      case "5%":
        return Math.round(baseSubtotal * 0.05);
      case "10%":
        return Math.round(baseSubtotal * 0.1);
      case "15%":
        return Math.round(baseSubtotal * 0.15);
      case "roundup":
        return Math.ceil(baseSubtotal / 10) * 10 - baseSubtotal || 10;
      default:
        return 0;
    }
  };

  const discount = appliedVoucher
    ? appliedVoucher.discountType === "fixed"
      ? appliedVoucher.discountValue
      : Math.round((baseSubtotal * appliedVoucher.discountValue) / 100)
    : 0;

  const tipAmount = getTipAmount();
  const totalAmount = Math.max(0, baseSubtotal - discount + tipAmount);
  const earnedPoints = 170;

  const handlePay = () => {
    onPaySuccess({
      ...initialDetails,
      customerName: name,
      customerPhone: `+47 ${phone}`,
      pickupMethod: pickupMethod === "counter" ? "Counter" : "Table",
      subtotal: baseSubtotal,
      tipAmount,
      discountAmount: discount,
      total: totalAmount,
      pointsToEarn: earnedPoints,
      appliedVoucher,
    });
  };

  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-32">
      {/* 1. TOP HEADER & 3-STEP BREADCRUMBS (Figma 06.07: <  1 Menu >  2 Cart >  3 Checkout) */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        {/* Nút quay lại giỏ hàng */}
        <button
          onClick={onBack}
          className="w-7 h-7 rounded-md border border-slate-200 bg-white flex items-center justify-center text-slate-800 hover:bg-slate-50 active:scale-95 transition-all shadow-2xs cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4 stroke-[2.2]" />
        </button>

        {/* 3 Step Breadcrumb (1, 2, 3 đều vàng cam ở bước Checkout) */}
        <div className="flex items-center gap-1.5 text-xs select-none">
          {/* Step 1: Menu */}
          <div className="flex items-center gap-1">
            <span className="w-4 h-4 rounded-full bg-[#E5A800] text-slate-950 text-[10px] font-extrabold flex items-center justify-center shadow-2xs">
              1
            </span>
            <span className="text-[11px] font-semibold text-slate-700">
              Menu
            </span>
          </div>
          <span className="text-slate-300 text-[10px] mx-0.5">{">"}</span>

          {/* Step 2: Cart */}
          <div className="flex items-center gap-1">
            <span className="w-4 h-4 rounded-full bg-[#E5A800] text-slate-950 text-[10px] font-extrabold flex items-center justify-center shadow-2xs">
              2
            </span>
            <span className="text-[11px] font-semibold text-slate-700">
              Cart
            </span>
          </div>
          <span className="text-slate-300 text-[10px] mx-0.5">{">"}</span>

          {/* Step 3: Checkout (Active) */}
          <div className="flex items-center gap-1">
            <span className="w-4 h-4 rounded-full bg-[#E5A800] text-slate-950 text-[10px] font-extrabold flex items-center justify-center shadow-2xs">
              3
            </span>
            <span className="text-[11px] font-extrabold text-slate-900">
              Checkout
            </span>
          </div>
        </div>
      </header>

      <div className="px-5 pt-4 space-y-5">
        {/* 2. TIÊU ĐỀ CHECK OUT (Có gạch chân chuẩn Figma 06.07) */}
        <div>
          <h1 className="text-xl font-extrabold text-slate-900 inline-block border-b-2 border-slate-900 pb-0.5 tracking-tight">
            Check out
          </h1>
        </div>

        {/* 3. PICKUP METHOD (2 Card xếp dọc chuẩn Figma 06.07) */}
        <div className="space-y-2">
          <h2 className="text-xs font-bold text-slate-900">Pickup Method</h2>

          {/* Card 1: Pickup at Counter */}
          <div
            onClick={() => setPickupMethod("counter")}
            className={`rounded-xl p-3 flex items-center gap-3 cursor-pointer transition-all ${
              pickupMethod === "counter"
                ? "border-2 border-[#E5A800] bg-[#FFF8E7] shadow-xs"
                : "border border-slate-200 bg-white hover:border-slate-300"
            }`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                pickupMethod === "counter"
                  ? "text-[#E5A800] bg-white shadow-2xs"
                  : "text-slate-600 bg-slate-50"
              }`}
            >
              <Store className="w-5 h-5 stroke-[1.8]" />
            </div>
            <div>
              <h3
                className={`text-xs font-bold ${
                  pickupMethod === "counter"
                    ? "text-[#DEA100]"
                    : "text-slate-900"
                }`}
              >
                Pickup at Counter
              </h3>
              <p className="text-[10px] text-slate-500 font-medium">
                Please go to our Counter to collect your order
              </p>
            </div>
          </div>

          {/* Card 2: Table */}
          <div
            onClick={() => setPickupMethod("table")}
            className={`rounded-xl p-3 flex items-center gap-3 cursor-pointer transition-all ${
              pickupMethod === "table"
                ? "border-2 border-[#E5A800] bg-[#FFF8E7] shadow-xs"
                : "border border-slate-200 bg-white hover:border-slate-300"
            }`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                pickupMethod === "table"
                  ? "text-[#E5A800] bg-white shadow-2xs"
                  : "text-slate-600 bg-slate-50"
              }`}
            >
              <UtensilsCrossed className="w-5 h-5 stroke-[1.8]" />
            </div>
            <div>
              <h3
                className={`text-xs font-bold ${
                  pickupMethod === "table" ? "text-[#DEA100]" : "text-slate-900"
                }`}
              >
                Table
              </h3>
              <p className="text-[10px] text-slate-400 font-medium">
                Select a table
              </p>
            </div>
          </div>
        </div>

        {/* 4. PICKUP TIME (2 Card xếp dọc chuẩn Figma 06.07) */}
        <div className="space-y-2">
          <h2 className="text-xs font-bold text-slate-900">Pickup Time</h2>

          {/* Card 1: As soon as possible */}
          <div
            onClick={() => setPickupTime("asap")}
            className={`rounded-xl p-3 flex items-center gap-3 cursor-pointer transition-all ${
              pickupTime === "asap"
                ? "border-2 border-[#E5A800] bg-[#FFF8E7] shadow-xs"
                : "border border-slate-200 bg-white hover:border-slate-300"
            }`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                pickupTime === "asap"
                  ? "text-[#E5A800] bg-white shadow-2xs"
                  : "text-slate-600 bg-slate-50"
              }`}
            >
              <Zap className="w-5 h-5 fill-current" />
            </div>
            <div>
              <h3
                className={`text-xs font-bold ${
                  pickupTime === "asap" ? "text-[#DEA100]" : "text-slate-900"
                }`}
              >
                As soon as possible
              </h3>
              <p className="text-[10px] text-slate-500 font-medium">
                Preparing time: 15 Minutes
              </p>
            </div>
          </div>

          {/* Card 2: Schedule */}
          <div
            onClick={() => setPickupTime("schedule")}
            className={`rounded-xl p-3 flex items-center gap-3 cursor-pointer transition-all ${
              pickupTime === "schedule"
                ? "border-2 border-[#E5A800] bg-[#FFF8E7] shadow-xs"
                : "border border-slate-200 bg-white hover:border-slate-300"
            }`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                pickupTime === "schedule"
                  ? "text-[#E5A800] bg-white shadow-2xs"
                  : "text-slate-600 bg-slate-50"
              }`}
            >
              <Calendar className="w-5 h-5 stroke-[1.8]" />
            </div>
            <div>
              <h3
                className={`text-xs font-bold ${
                  pickupTime === "schedule"
                    ? "text-[#DEA100]"
                    : "text-slate-900"
                }`}
              >
                Schedule
              </h3>
              <p className="text-[10px] text-slate-400 font-medium">
                Select a time
              </p>
            </div>
          </div>
        </div>

        {/* 5. TIPS (4 nút + 1 radio Not today chuẩn Figma 06.07) */}
        <div className="space-y-2.5">
          <h2 className="text-xs font-bold text-slate-900">Tips</h2>

          <div className="grid grid-cols-4 gap-2">
            {(["5%", "10%", "15%", "roundup"] as TipOption[]).map((tip) => (
              <button
                key={tip}
                type="button"
                onClick={() => setSelectedTip(tip)}
                className={`py-2 rounded-md border text-xs font-semibold text-center transition-all cursor-pointer ${
                  selectedTip === tip
                    ? "border-[#E5A800] bg-[#FFF8E7] text-[#DEA100] font-bold shadow-2xs"
                    : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
                }`}
              >
                {tip === "roundup" ? "Roundup" : tip}
              </button>
            ))}
          </div>

          {/* Radio Not today */}
          <div
            onClick={() => setSelectedTip("none")}
            className="flex items-center gap-2 cursor-pointer pt-1"
          >
            <div
              className={`w-4 h-4 rounded-full border flex items-center justify-center transition-colors ${
                selectedTip === "none"
                  ? "border-slate-800 bg-white"
                  : "border-slate-300 bg-white"
              }`}
            >
              {selectedTip === "none" && (
                <div className="w-2 h-2 rounded-full bg-slate-800" />
              )}
            </div>
            <span className="text-xs font-medium text-slate-600">
              Not today
            </span>
          </div>
        </div>

        {/* 6. ADD PROMO CODE (Dòng đơn có icon tag chuẩn Figma 06.07) */}
        <div
          onClick={() => setIsOffersDrawerOpen(true)}
          className="flex items-center justify-between py-2 border-t border-slate-100 cursor-pointer hover:bg-slate-50 transition-colors"
        >
          <div className="flex items-center gap-2">
            <Tag className="w-4 h-4 text-slate-900 fill-slate-900" />
            <span className="text-xs font-bold text-slate-900">
              {appliedVoucher
                ? `Voucher: ${appliedVoucher.code}`
                : "Add promo code"}
            </span>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-400" />
        </div>

        {/* 7. WHO WILL PICKUP THE ORDER? (Chuẩn Figma 06.07) */}
        <div className="space-y-2 pt-1 border-t border-slate-100">
          <h2 className="text-xs font-bold text-slate-900">
            Who will pickup the order?
          </h2>
          <p className="text-[10px] text-slate-400 font-medium">
            Enter phone number to register loyalty account & redeem points
          </p>

          {/* Ô nhập tên có nút xoá (x) */}
          <div className="rounded-md border border-slate-200 bg-white px-3 py-2 flex items-center justify-between shadow-2xs">
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Your name"
              className="bg-transparent border-none outline-none text-xs font-semibold text-slate-800 w-full"
            />
            {name && (
              <button
                onClick={() => setName("")}
                className="text-slate-400 hover:text-slate-600 cursor-pointer ml-1"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Ô nhập số điện thoại +47 */}
          <div className="flex gap-2">
            <div className="px-3 py-2 rounded-md border border-slate-200 bg-white text-xs font-semibold text-slate-700 shrink-0 shadow-2xs">
              +47
            </div>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value.replace(/[^0-9]/g, ""))}
              placeholder="97362748"
              className="flex-1 px-3 py-2 rounded-md border border-slate-200 bg-white text-xs font-semibold text-slate-800 focus:outline-hidden focus:border-amber-500 shadow-2xs"
            />
          </div>

          {/* Checkbox điều khoản */}
          <label className="flex items-center gap-2 pt-1 cursor-pointer">
            <input
              type="checkbox"
              checked={agreeTerms}
              onChange={(e) => setAgreeTerms(e.target.checked)}
              className="w-3.5 h-3.5 rounded border-slate-300 text-amber-500 focus:ring-amber-400 cursor-pointer"
            />
            <span className="text-[9px] text-slate-400 font-medium">
              Agree to register a loyalty account &{" "}
              <span className="underline">Terms of Services</span>
            </span>
          </label>
        </div>

        {/* 8. SUMMARY & PAY BUTTON (Chuẩn Figma 06.07) */}
        <div className="pt-4 pb-8 space-y-3">
          {/* Dòng Subtotal */}
          <div className="flex items-center justify-between text-xs pt-3 border-t border-slate-100">
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-slate-600">Subtotal</span>
              <span className="text-[10px] text-slate-400">(included VAT)</span>
            </div>
            <span className="text-xs font-bold text-slate-900">
              {baseSubtotal} Kr
            </span>
          </div>

          {/* Dòng Total kèm badge +170 points */}
          <div className="flex items-center justify-between pt-1">
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-base text-slate-900">
                Total
              </span>
              <span className="bg-red-50 text-red-500 font-bold text-[10px] px-2 py-0.5 rounded-full">
                +{earnedPoints} points
              </span>
            </div>
            <span className="font-extrabold text-lg text-slate-900">
              {totalAmount} Kr
            </span>
          </div>

          {/* Nút Pay màu vàng cam có nút tròn số 3 */}
          <button
            onClick={handlePay}
            className="w-full py-3.5 px-4 rounded-xl bg-[#F5B843] hover:bg-[#DEA100] active:scale-[0.99] text-slate-900 flex items-center justify-between font-bold text-sm shadow-xs transition-all cursor-pointer mt-3"
          >
            <div className="w-5 h-5 rounded-full border border-slate-900 flex items-center justify-center text-[10px] font-bold">
              3
            </div>
            <span className="font-extrabold tracking-tight">Pay</span>
            <span className="font-extrabold">{totalAmount} Kr</span>
          </button>
        </div>
      </div>

      {/* DRAWER ÁP MÃ & QUÀ TẶNG (Figma 08.02 / 08.04) */}
      <MyOffersDrawer
        isOpen={isOffersDrawerOpen}
        onClose={() => setIsOffersDrawerOpen(false)}
        vouchers={MOCK_MY_VOUCHERS}
        gifts={MOCK_MY_GIFTS}
        selectedVoucher={appliedVoucher}
        selectedGift={null}
        onSelectVoucher={(v: VoucherItem | null) => {
          setAppliedVoucher(v);
          setIsOffersDrawerOpen(false);
        }}
        onSelectGift={() => setIsOffersDrawerOpen(false)}
        onApplyManualCode={(code: string) => {
          setAppliedVoucher({
            id: "manual-" + code,
            code,
            title: `Mã ${code}`,
            discountValue: 30,
            discountType: "fixed",
            minOrderValue: 0,
            expiryDate: "31/12/2026",
            description: "Áp dụng thành công",
          });
          setIsOffersDrawerOpen(false);
        }}
      />

      {/* MODAL NHẬP MÃ (Figma 08.05) */}
      <PromoCodeModal
        isOpen={isPromoModalOpen}
        onClose={() => setIsPromoModalOpen(false)}
        onApply={(code: string) => {
          setAppliedVoucher({
            id: "manual-" + code,
            code,
            title: `Mã ${code}`,
            discountValue: 30,
            discountType: "fixed",
            minOrderValue: 0,
            expiryDate: "31/12/2026",
            description: "Áp dụng thành công",
          });
          return true;
        }}
      />
    </div>
  );
};
