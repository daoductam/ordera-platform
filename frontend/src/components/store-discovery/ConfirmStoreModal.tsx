import React from "react";
import { Store } from "lucide-react";
import type { Restaurant } from "@/types/store-discovery";

export interface ConfirmStoreModalProps {
  isOpen: boolean;
  restaurant: Restaurant | null;
  onConfirm: () => void;
  onChangeShop: () => void;
}

export const ConfirmStoreModal: React.FC<ConfirmStoreModalProps> = ({
  isOpen,
  restaurant,
  onConfirm,
  onChangeShop,
}) => {
  if (!isOpen || !restaurant) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white w-full max-w-sm rounded-3xl p-6 text-center space-y-4 shadow-2xl animate-in zoom-in-95">
        <div className="w-14 h-14 bg-amber-100 text-amber-600 rounded-full flex items-center justify-center mx-auto shadow-inner">
          <Store className="w-7 h-7" />
        </div>

        <div className="space-y-1.5">
          <h3 className="font-extrabold text-xl text-slate-900">Just making sure...</h3>
          <p className="text-xs text-slate-600 leading-relaxed px-2">
            You've selected <strong className="text-slate-900">{restaurant.name}</strong> (
            <span className="text-slate-500">{restaurant.address}</span>) for your order.
            Is this the restaurant/shop you want to order from?
          </p>
        </div>

        <div className="pt-2 space-y-2">
          <button
            onClick={onConfirm}
            className="w-full py-3.5 bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-900 rounded-2xl font-black text-sm shadow-md shadow-amber-500/20 transition-all"
          >
            Yes, that's the one
          </button>
          <button
            onClick={onChangeShop}
            className="w-full py-2.5 text-xs font-bold text-slate-500 hover:text-slate-800 transition-colors"
          >
            Change Shop
          </button>
        </div>
      </div>
    </div>
  );
};

