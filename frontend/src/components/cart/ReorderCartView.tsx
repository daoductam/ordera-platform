import React, { useState } from "react";
import { ArrowLeft, SquarePen, Minus, Plus } from "lucide-react";

export interface CartItem {
  id: string;
  name: string;
  size?: string;
  toppings?: string[];
  sweetness?: string;
  ice?: string;
  pricePerUnit: number;
  quantity: number;
}

export interface ReorderCartViewProps {
  onBack: () => void;
  onProceedToCheckout: (cartData: {
    items: CartItem[];
    comment: string;
    subtotal: number;
  }) => void;
}

export const ReorderCartView: React.FC<ReorderCartViewProps> = ({
  onBack,
  onProceedToCheckout,
}) => {
  // Dữ liệu mock chuẩn 100% theo ảnh Figma 06.06 Giỏ hàng · Đặt lại
  const [items, setItems] = useState<CartItem[]>([
    {
      id: "item-1",
      name: "Aurora",
      size: "L",
      toppings: ["Mango Popping", "Strawberry Popping", "Tapioca"],
      sweetness: "Standard",
      ice: "Less ice",
      pricePerUnit: 89,
      quantity: 2, // 89 * 2 = 178 Kr
    },
    {
      id: "item-2",
      name: "Tuna Maki",
      size: "L",
      toppings: ["Mango Popping", "Strawberry Popping", "Tapioca"],
      sweetness: "Standard",
      ice: "Less ice",
      pricePerUnit: 89,
      quantity: 1, // 89 Kr
    },
  ]);

  const [comment, setComment] = useState("");

  const updateQuantity = (id: string, delta: number) => {
    setItems((prev) =>
      prev
        .map((item) => {
          if (item.id === id) {
            const nextQty = Math.max(1, item.quantity + delta);
            return { ...item, quantity: nextQty };
          }
          return item;
        })
    );
  };

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = items.reduce(
    (sum, item) => sum + item.pricePerUnit * item.quantity,
    0
  );

  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-28 flex flex-col justify-between">
      <div>
        {/* 1. TOP HEADER WITH STEP BREADCRUMBS (Figma 06.06: <  1 Menu >  2 Cart >  3 Checkout) */}
        <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
          {/* Back button */}
          <button
            onClick={onBack}
            className="w-7 h-7 rounded-md border border-slate-200 bg-white flex items-center justify-center text-slate-800 hover:bg-slate-50 active:scale-95 transition-all shadow-2xs cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4 stroke-[2.2]" />
          </button>

          {/* Stepper breadcrumbs */}
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

            {/* Step 2: Cart (Active) */}
            <div className="flex items-center gap-1">
              <span className="w-4 h-4 rounded-full bg-[#E5A800] text-slate-950 text-[10px] font-extrabold flex items-center justify-center shadow-2xs">
                2
              </span>
              <span className="text-[11px] font-extrabold text-slate-900">
                Cart
              </span>
            </div>
            <span className="text-slate-300 text-[10px] mx-0.5">{">"}</span>

            {/* Step 3: Checkout */}
            <div className="flex items-center gap-1 opacity-60">
              <span className="w-4 h-4 rounded-full bg-slate-100 border border-slate-300 text-slate-500 text-[10px] font-bold flex items-center justify-center">
                3
              </span>
              <span className="text-[11px] font-medium text-slate-500">
                Checkout
              </span>
            </div>
          </div>
        </header>

        {/* 2. TIÊU ĐỀ YOUR CART (Có gạch chân chuẩn Figma 06.06) */}
        <div className="px-5 pt-4 pb-2">
          <h1 className="text-xl font-extrabold text-slate-900 inline-block border-b-2 border-slate-900 pb-0.5 tracking-tight">
            Your Cart
          </h1>
        </div>

        {/* 3. SỐ LƯỢNG MÓN (3 items) */}
        <div className="px-5 pt-1 pb-2">
          <h2 className="text-sm font-bold text-slate-900">{totalItems} items</h2>
        </div>

        {/* 4. DANH SÁCH MÓN ĐẶT LẠI */}
        <div className="px-5 divide-y divide-slate-100">
          {items.map((item) => (
            <div key={item.id} className="py-3.5 space-y-2">
              {/* Header dòng món: Tên món & Icon sửa */}
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-sm text-slate-900 tracking-tight">
                  {item.name}
                </h3>
                <button
                  title="Edit item options"
                  className="text-amber-500 hover:text-amber-600 active:scale-95 transition-transform cursor-pointer p-0.5"
                >
                  <SquarePen className="w-4 h-4 stroke-[2]" />
                </button>
              </div>

              {/* Chi tiết tùy chọn món: Size, Topping, Sweetness, Ice */}
              <div className="text-[10px] text-slate-500 space-y-0.5 leading-relaxed font-medium">
                {item.size && <div>Size: {item.size}</div>}
                {item.toppings && (
                  <div>Topping: {item.toppings.join(", ")}</div>
                )}
                {item.sweetness && (
                  <div>Sweetness Level: {item.sweetness}</div>
                )}
                {item.ice && <div>Ice Level: {item.ice}</div>}
              </div>

              {/* Hàng dưới: Bộ tăng giảm số lượng & Giá tiền */}
              <div className="flex items-center justify-between pt-1">
                {/* Nút - và + màu vàng hổ phách chuẩn Figma */}
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => updateQuantity(item.id, -1)}
                    className="w-5 h-5 rounded-full bg-[#E5A800] text-slate-950 flex items-center justify-center font-bold text-xs hover:bg-amber-400 active:scale-90 transition-all cursor-pointer shadow-2xs"
                  >
                    <Minus className="w-3 h-3 stroke-[3]" />
                  </button>
                  <span className="text-xs font-bold text-slate-900 min-w-4 text-center">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() => updateQuantity(item.id, 1)}
                    className="w-5 h-5 rounded-full bg-[#E5A800] text-slate-950 flex items-center justify-center font-bold text-xs hover:bg-amber-400 active:scale-90 transition-all cursor-pointer shadow-2xs"
                  >
                    <Plus className="w-3 h-3 stroke-[3]" />
                  </button>
                </div>

                {/* Giá tiền */}
                <div className="text-sm font-bold text-slate-900">
                  {item.pricePerUnit * item.quantity} Kr
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 5. GHI CHÚ ĐƠN HÀNG (Add a comment for your order • 0/200) */}
        <div className="px-5 pt-4">
          <div className="flex items-center justify-between mb-1.5">
            <label className="text-xs font-bold text-slate-900">
              Add a comment for your order
            </label>
            <span className="text-[10px] text-slate-400 font-medium">
              {comment.length}/200
            </span>
          </div>
          <textarea
            maxLength={200}
            rows={3}
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Special request, allergies, etc"
            className="w-full p-3 rounded-lg border border-slate-300 text-xs text-slate-800 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 resize-none font-medium transition-colors shadow-2xs"
          />
        </div>
      </div>

      {/* 6. BOTTOM SUMMARY & CHECK OUT BUTTON (Chuẩn Figma 06.06) */}
      <div className="sticky bottom-0 bg-white border-t border-slate-100 p-4 shadow-[0_-4px_16px_rgba(0,0,0,0.06)] z-20">
        <div className="flex items-center justify-between text-xs mb-3">
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-slate-700">Subtotal</span>
            <span className="text-[10px] text-slate-400">(included VAT)</span>
          </div>
          <span className="text-sm font-bold text-slate-900">{subtotal} Kr</span>
        </div>

        <button
          onClick={() =>
            onProceedToCheckout({
              items,
              comment,
              subtotal,
            })
          }
          className="w-full py-3.5 px-4 rounded-xl bg-[#F5B843] hover:bg-[#DEA100] active:scale-[0.99] text-slate-900 flex items-center justify-between font-bold text-sm shadow-xs transition-all cursor-pointer"
        >
          <div className="w-5 h-5 rounded-full border border-slate-900 flex items-center justify-center text-[10px] font-bold">
            3
          </div>
          <span className="font-extrabold tracking-tight">Check out</span>
          <span className="font-extrabold">{subtotal} Kr</span>
        </button>
      </div>
    </div>
  );
};

