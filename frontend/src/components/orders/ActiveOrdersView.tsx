import React from "react";
import { Clock, RotateCcw, FileText } from "lucide-react";
import type { CustomerOrder } from "@/types/order";

export interface ActiveOrdersViewProps {
  orders: CustomerOrder[];
  onOpenDetails: (order: CustomerOrder) => void;
  onReorder: (order: CustomerOrder) => void;
  onSwitchToHistory: () => void;
}

export const ActiveOrdersView: React.FC<ActiveOrdersViewProps> = ({
  orders,
  onOpenDetails,
  onReorder,
  onSwitchToHistory,
}) => {
  return (
    <div className="space-y-4">
      {/* 2 Tabs: Active Orders (Active) / Past Orders */}
      <div className="flex bg-slate-100 p-1 rounded-2xl">
        <button
          type="button"
          className="flex-1 py-2 text-xs font-black rounded-xl bg-white text-slate-950 shadow-xs transition-all"
        >
          Active Orders
        </button>
        <button
          type="button"
          onClick={onSwitchToHistory}
          className="flex-1 py-2 text-xs font-bold rounded-xl text-slate-500 hover:text-slate-800 transition-all"
        >
          Past Orders
        </button>
      </div>

      {/* Danh sách các đơn đang hoạt động (Figma 06.02) */}
      <div className="space-y-3">
        {orders.map((order) => (
          <div
            key={order.id}
            className="bg-white rounded-3xl p-4 border border-slate-100 shadow-2xs space-y-3"
          >
            {/* Top row: Shop cover/name + Giá tiền */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-11 h-11 rounded-2xl bg-amber-500 text-slate-950 font-black text-base flex items-center justify-center shrink-0 shadow-xs">
                  🍱
                </div>
                <div>
                  <h4 className="font-extrabold text-sm text-slate-900">{order.restaurantName}</h4>
                  <span className="text-[11px] font-semibold text-emerald-600 flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    <span>Đang chuẩn bị món</span>
                  </span>
                </div>
              </div>
              <span className="font-black text-sm text-slate-900">
                {order.totalAmount} NOK
              </span>
            </div>

            {/* Thông tin đơn: Mã số, thời gian, tên món */}
            <div className="bg-slate-50/80 rounded-2xl p-3 text-xs space-y-1 border border-slate-100">
              <div className="flex justify-between text-slate-500 text-[11px]">
                <span className="font-bold text-slate-700">Order number: {order.orderNumber}</span>
                <span>Placed at {order.placedAt}</span>
              </div>
              <p className="font-medium text-slate-800 line-clamp-1 pt-0.5">
                {order.items.map((i) => i.name).join("; ")}
              </p>
            </div>

            {/* 2 Nút hành động: Details & Reorder (Figma 06.02) */}
            <div className="flex gap-2.5 pt-0.5">
              <button
                onClick={() => onOpenDetails(order)}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-800 font-bold text-xs flex items-center justify-center gap-1.5 transition-all active:scale-98"
              >
                <FileText className="w-3.5 h-3.5" />
                <span>Details</span>
              </button>

              <button
                onClick={() => onReorder(order)}
                className="flex-1 py-2.5 rounded-xl bg-amber-400 hover:bg-amber-500 text-slate-950 font-black text-xs flex items-center justify-center gap-1.5 transition-all active:scale-98 shadow-2xs"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Reorder</span>
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
