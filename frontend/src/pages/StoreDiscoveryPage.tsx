import React, { useState } from "react";
import { Utensils, ShoppingBag, Bike, Store } from "lucide-react";
import type { Restaurant, OrderOptions } from "@/types/store-discovery";
import { DEFAULT_ORDER_OPTIONS, MOCK_RESTAURANTS } from "@/mock/storeDiscoveryMock";
import { OrderOptionsModal } from "@/components/store-discovery/OrderOptionsModal";
import { RestaurantCard } from "@/components/store-discovery/RestaurantCard";
import { ConfirmStoreModal } from "@/components/store-discovery/ConfirmStoreModal";
import { MenuScreen } from "@/components/store-discovery/MenuScreen";

export const StoreDiscoveryPage: React.FC = () => {
  // Trạng thái đơn hàng (Dine In / Takeaway / Delivery)
  const [orderOptions, setOrderOptions] = useState<OrderOptions>(DEFAULT_ORDER_OPTIONS);
  const [isOptionsModalOpen, setIsOptionsModalOpen] = useState(false);

  // Mặc định chọn màn hình Thực đơn 01.06 để người dùng xem ngay kết quả
  const [currentFrame, setCurrentFrame] = useState<"01.01" | "01.02" | "01.05" | "01.06">("01.06");
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant>(MOCK_RESTAURANTS[0]);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const handleSelectRestaurant = (restaurant: Restaurant) => {
    setSelectedRestaurant(restaurant);
    setShowConfirmModal(true);
  };

  const handleConfirmStore = () => {
    setShowConfirmModal(false);
    setCurrentFrame("01.06");
  };

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center">
      <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl">
        {/* NẾU ĐANG Ở MÀN 01.06 THỰC ĐƠN */}
        {currentFrame === "01.06" ? (
          <MenuScreen
            restaurant={selectedRestaurant}
            orderOptions={orderOptions}
            onBack={() => setCurrentFrame("01.02")}
            onUpdateOrderOptions={(opts) => setOrderOptions(opts)}
          />
        ) : (
          /* MÀN 01.02 DANH SÁCH CỬA HÀNG */
          <div className="bg-slate-50 min-h-screen p-4 space-y-4 pb-24">
            {/* 1. BRAND WELCOME HEADER */}
            <div className="bg-white rounded-3xl p-4 border border-slate-100 shadow-xs flex items-center gap-3.5">
              <div className="w-12 h-12 rounded-2xl bg-amber-500 text-slate-950 font-black text-xl flex items-center justify-center shrink-0 shadow-sm">
                🍣
              </div>
              <div className="flex-1 min-w-0">
                <span className="text-[10px] font-black uppercase text-amber-600 tracking-wider">
                  Ordera Store Discovery
                </span>
                <h2 className="font-extrabold text-base text-slate-900 truncate">
                  Welcome to Linn Sushi
                </h2>
                <p className="text-xs text-slate-500 truncate">
                  Where fresh sushi meets your taste
                </p>
              </div>
            </div>

            {/* 2. ORDER OPTIONS BANNER */}
            <div className="bg-white rounded-2xl p-3.5 border border-slate-100 shadow-xs flex items-center justify-between gap-3">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center shrink-0">
                  {orderOptions.method === "Dine In" && <Utensils className="w-4 h-4" />}
                  {orderOptions.method === "Takeaway" && <ShoppingBag className="w-4 h-4" />}
                  {orderOptions.method === "Delivery" && <Bike className="w-4 h-4" />}
                </div>
                <div>
                  <h4 className="font-extrabold text-xs text-slate-900">
                    {orderOptions.method}
                  </h4>
                  <p className="text-[11px] text-slate-500">
                    {orderOptions.timing === "asap"
                      ? "As soon as possible"
                      : `Scheduled: ${orderOptions.scheduledTime || "Today"}`}
                  </p>
                </div>
              </div>

              <button
                onClick={() => setIsOptionsModalOpen(true)}
                className="px-3.5 py-1.5 rounded-xl border border-amber-400 bg-amber-50/60 hover:bg-amber-100 text-amber-900 text-xs font-black transition-all active:scale-95"
              >
                Change
              </button>
            </div>

            {/* 3. SELECT RESTAURANT HEADER */}
            <div className="flex items-center justify-between pt-1">
              <h3 className="font-extrabold text-base text-slate-900 flex items-center gap-1.5">
                <Store className="w-4 h-4 text-amber-600" />
                Select Restaurant:
              </h3>
              <span className="text-xs text-slate-400 font-semibold">
                {MOCK_RESTAURANTS.length} locations
              </span>
            </div>

            {/* 4. RESTAURANT CARDS LIST */}
            <div className="space-y-4">
              {MOCK_RESTAURANTS.map((restaurant) => (
                <RestaurantCard
                  key={restaurant.id}
                  restaurant={restaurant}
                  onSelect={handleSelectRestaurant}
                />
              ))}
            </div>
          </div>
        )}

        {/* MODAL 1: CHANGE ORDER OPTIONS (01.01 & 01.03) */}
        <OrderOptionsModal
          isOpen={isOptionsModalOpen}
          onClose={() => setIsOptionsModalOpen(false)}
          currentOptions={orderOptions}
          onSave={(newOptions) => setOrderOptions(newOptions)}
        />

        {/* MODAL 2: CONFIRM STORE (01.05 "Just making sure...") */}
        <ConfirmStoreModal
          isOpen={showConfirmModal}
          restaurant={selectedRestaurant}
          onConfirm={handleConfirmStore}
          onChangeShop={() => setShowConfirmModal(false)}
        />
      </div>
    </div>
  );
};
