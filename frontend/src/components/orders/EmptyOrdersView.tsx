import React from "react";
import { ShoppingBag, ArrowRight } from "lucide-react";

export interface EmptyOrdersViewProps {
  onGoToShop: () => void;
  onViewHistory?: () => void;
}

export const EmptyOrdersView: React.FC<EmptyOrdersViewProps> = ({
  onGoToShop,
  onViewHistory,
}) => {
  return (
    <div className="bg-white rounded-3xl p-8 border border-slate-100 shadow-2xs text-center space-y-4 my-6">
      <div className="w-16 h-16 rounded-3xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto shadow-inner">
        <ShoppingBag className="w-8 h-8 stroke-[1.5]" />
      </div>

      <div className="space-y-1">
        <h3 className="font-black text-base text-slate-900">No orders</h3>
        <p className="text-xs text-slate-500">You have no active orders</p>
      </div>

      <div className="pt-2 space-y-2">
        <button
          onClick={onGoToShop}
          className="w-full py-3 rounded-2xl bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-950 font-black text-xs shadow-xs transition-all flex items-center justify-center gap-1.5"
        >
          <span>Go to Shop</span>
          <ArrowRight className="w-4 h-4 stroke-[2.5]" />
        </button>

        {onViewHistory && (
          <button
            onClick={onViewHistory}
            className="w-full py-2.5 rounded-2xl border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold text-xs transition-all"
          >
            View Order History
          </button>
        )}
      </div>
    </div>
  );
};

