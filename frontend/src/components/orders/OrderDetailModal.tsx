import React from "react";
import { X, Receipt, RotateCcw, Star, Store, Clock, Utensils } from "lucide-react";
import type { CustomerOrder } from "@/types/order";

export interface OrderDetailModalProps {
  isOpen: boolean;
  order: CustomerOrder | null;
  onClose: () => void;
  onGetReceipt: (order: CustomerOrder) => void;
  onReorder: (order: CustomerOrder) => void;
}

export const OrderDetailModal: React.FC<OrderDetailModalProps> = ({
  isOpen,
  order,
  onClose,
  onGetReceipt,
  onReorder,
}) => {
  if (!isOpen || !order) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[92vh] flex flex-col animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="font-extrabold text-base text-slate-900">Order Details</h3>
            <span className="text-[11px] font-bold text-amber-600">
              {order.customerName} ({order.orderNumber})
            </span>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <div className="p-5 space-y-4 overflow-y-auto flex-1 text-xs text-slate-800">
          {/* Status & Total Summary Card */}
          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 flex items-center justify-between">
            <div>
              <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">
                Total (incl. VAT)
              </span>
              <span className="text-xl font-black text-slate-900 block mt-0.5">
                {order.totalAmount.toFixed(2).replace(".", ",")} NOK
              </span>
            </div>
            <span className="px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 font-bold text-[10px]">
              {order.status === "preparing" ? "Preparing" : "Completed"}
            </span>
          </div>

          {/* Chi tiết đơn hàng (Place at, Restaurant, Delivery method) */}
          <div className="bg-white rounded-2xl border border-slate-100 divide-y divide-slate-100 shadow-2xs overflow-hidden">
            <div className="p-3 flex items-center justify-between">
              <span className="text-slate-500 font-medium flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-slate-400" /> Place at
              </span>
              <span className="font-bold text-slate-900">{order.placedAt}</span>
            </div>

            <div className="p-3 flex items-center justify-between">
              <span className="text-slate-500 font-medium flex items-center gap-1.5">
                <Store className="w-3.5 h-3.5 text-slate-400" /> Restaurant/shop
              </span>
              <span className="font-bold text-slate-900">{order.restaurantName}</span>
            </div>

            <div className="p-3 flex items-center justify-between">
              <span className="text-slate-500 font-medium flex items-center gap-1.5">
                <Utensils className="w-3.5 h-3.5 text-slate-400" /> Delivery method
              </span>
              <span className="font-bold text-slate-900">{order.pickupMethod}</span>
            </div>
          </div>

          {/* Danh sách món ăn (Items details) */}
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <h4 className="font-black text-xs text-slate-900">
                Items details ({order.items.length} items)
              </h4>
            </div>

            <div className="space-y-2">
              {order.items.map((item) => (
                <div
                  key={item.id}
                  className="p-3 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between gap-3"
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <span className="w-5 h-5 rounded-md bg-white text-slate-900 font-extrabold text-[11px] flex items-center justify-center shrink-0 border border-slate-200">
                      {item.quantity}x
                    </span>
                    <span className="font-bold text-slate-900 truncate">{item.name}</span>
                  </div>
                  <span className="font-black text-slate-900 shrink-0">
                    {item.totalPrice.toFixed(2).replace(".", ",")} NOK
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* 4 Nút hành động chuẩn Figma 06.05: Get receipt • Review • Reorder • Cancel */}
          <div className="grid grid-cols-2 gap-2 pt-2">
            <button
              onClick={() => onGetReceipt(order)}
              className="py-2.5 px-3 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-800 font-bold text-xs flex items-center justify-center gap-1.5 transition-all active:scale-98"
            >
              <Receipt className="w-3.5 h-3.5 text-amber-600" />
              <span>Get receipt</span>
            </button>

            <button
              onClick={() => alert("Tính năng đánh giá nhà hàng sẽ sớm mở!")}
              className="py-2.5 px-3 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-800 font-bold text-xs flex items-center justify-center gap-1.5 transition-all active:scale-98"
            >
              <Star className="w-3.5 h-3.5 text-amber-500" />
              <span>Review</span>
            </button>

            <button
              onClick={() => onReorder(order)}
              className="col-span-2 py-3 rounded-2xl bg-amber-400 hover:bg-amber-500 text-slate-950 font-black text-xs flex items-center justify-center gap-1.5 transition-all active:scale-98 shadow-xs"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Reorder Entire Meal</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
