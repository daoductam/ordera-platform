import React, { useState } from "react";
import { MapPin, ChevronDown, Sparkles, Store, ShoppingBag } from "lucide-react";
import { Link } from "react-router-dom";
import { MOCK_CURRENT_USER, MOCK_STORES } from "@/mock/mockData";
import { formatPoints } from "@/lib/utils";

export const Navbar: React.FC = () => {
  const [selectedStore, setSelectedStore] = useState(MOCK_STORES[0]);
  const [showStoreModal, setShowStoreModal] = useState(false);
  const [orderType, setOrderType] = useState<"TAKEAWAY" | "DELIVERY">("TAKEAWAY");

  return (
    <>
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-100 px-4 py-3 shadow-xs">
        <div className="flex items-center justify-between gap-3">
          {/* Store Selector */}
          <button
            onClick={() => setShowStoreModal(true)}
            className="flex items-center gap-2 text-left flex-1 min-w-0"
          >
            <div className="w-9 h-9 rounded-full bg-orange-50 flex items-center justify-center text-orange-600 shrink-0">
              <MapPin className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-1">
                <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  {orderType === "TAKEAWAY" ? "Mang đi (Takeaway)" : "Giao hàng (Delivery)"}
                </span>
                <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
              </div>
              <p className="text-sm font-bold text-slate-900 truncate">
                {selectedStore.name}
              </p>
            </div>
          </button>

          {/* Points Badge */}
          <Link
            to="/tier"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-linear-to-r from-amber-500 to-yellow-500 text-white shadow-sm hover:opacity-95 transition-transform active:scale-95 shrink-0"
          >
            <Sparkles className="w-3.5 h-3.5 fill-white" />
            <span className="text-xs font-black tracking-tight">
              {formatPoints(MOCK_CURRENT_USER.currentPoints)}
            </span>
            <span className="text-[10px] uppercase font-bold opacity-90">Điểm</span>
          </Link>
        </div>

        {/* Takeaway / Delivery Switcher */}
        <div className="flex mt-2 p-0.5 bg-slate-100 rounded-lg text-xs font-semibold text-slate-600">
          <button
            onClick={() => setOrderType("TAKEAWAY")}
            className={`flex-1 py-1.5 rounded-md flex items-center justify-center gap-1.5 transition-all ${
              orderType === "TAKEAWAY"
                ? "bg-white text-orange-600 font-bold shadow-xs"
                : "hover:text-slate-900"
            }`}
          >
            <ShoppingBag className="w-3.5 h-3.5" />
            Tại quầy / Mang đi
          </button>
          <button
            onClick={() => setOrderType("DELIVERY")}
            className={`flex-1 py-1.5 rounded-md flex items-center justify-center gap-1.5 transition-all ${
              orderType === "DELIVERY"
                ? "bg-white text-orange-600 font-bold shadow-xs"
                : "hover:text-slate-900"
            }`}
          >
            <Store className="w-3.5 h-3.5" />
            Giao hàng tận nơi
          </button>
        </div>
      </header>

      {/* Modal Chọn Cửa Hàng (01.02 Danh sách cửa hàng) */}
      {showStoreModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in">
          <div className="bg-white w-full max-w-md rounded-t-2xl sm:rounded-2xl p-5 max-h-[80vh] flex flex-col shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="font-bold text-base text-slate-900">Chọn cửa hàng Ordera</h3>
              <button
                onClick={() => setShowStoreModal(false)}
                className="text-slate-400 hover:text-slate-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>
            <div className="divide-y divide-slate-100 overflow-y-auto my-2">
              {MOCK_STORES.map((store) => (
                <button
                  key={store.id}
                  onClick={() => {
                    setSelectedStore(store);
                    setShowStoreModal(false);
                  }}
                  className={`w-full text-left py-3.5 px-2 flex items-start justify-between rounded-xl transition-colors ${
                    selectedStore.id === store.id ? "bg-orange-50/70" : "hover:bg-slate-50"
                  }`}
                >
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">{store.name}</h4>
                    <p className="text-xs text-slate-500 mt-0.5">{store.address}</p>
                    <span className="inline-block mt-1 text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
                      Mở cửa • Cách bạn {store.distanceKm} km
                    </span>
                  </div>
                  {selectedStore.id === store.id && (
                    <span className="text-orange-600 text-sm font-bold">✓</span>
                  )}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

