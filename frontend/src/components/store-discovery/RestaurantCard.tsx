import React, { useState } from "react";
import { Heart, Clock, MapPin } from "lucide-react";
import type { Restaurant } from "@/types/store-discovery";

export interface RestaurantCardProps {
  restaurant: Restaurant;
  onSelect: (restaurant: Restaurant) => void;
  onToggleFavorite?: (id: string) => void;
}

export const RestaurantCard: React.FC<RestaurantCardProps> = ({
  restaurant,
  onSelect,
  onToggleFavorite,
}) => {
  const [isFav, setIsFav] = useState(restaurant.isFavorite || false);

  const handleHeartClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsFav(!isFav);
    if (onToggleFavorite) onToggleFavorite(restaurant.id);
  };

  return (
    <div
      onClick={() => onSelect(restaurant)}
      className="bg-white rounded-3xl p-3 border border-slate-100 shadow-xs hover:shadow-md transition-all cursor-pointer group space-y-3"
    >
      {/* Hero Image with Heart Icon */}
      <div className="relative w-full h-44 rounded-2xl overflow-hidden bg-slate-100">
        <img
          src={restaurant.image}
          alt={restaurant.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        />
        <button
          onClick={handleHeartClick}
          className="absolute top-2.5 right-2.5 w-9 h-9 rounded-full bg-white/90 backdrop-blur-xs flex items-center justify-center text-slate-400 hover:text-red-500 transition-colors shadow-xs active:scale-90"
        >
          <Heart
            className={`w-5 h-5 transition-colors ${
              isFav ? "fill-red-500 text-red-500" : "stroke-[2]"
            }`}
          />
        </button>
      </div>

      {/* Info Section */}
      <div className="px-1 space-y-2">
        <div className="flex justify-between items-start gap-2">
          <div>
            <h4 className="font-extrabold text-base text-slate-900 group-hover:text-amber-600 transition-colors">
              {restaurant.name}
            </h4>
            <p className="text-xs text-slate-500 flex items-center gap-1 mt-0.5">
              <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span className="truncate">{restaurant.address}</span>
            </p>
          </div>
        </div>

        {/* Operating Hours & Tags */}
        <div className="flex flex-wrap items-center gap-2 pt-1 border-t border-slate-100 text-xs">
          <span className="inline-flex items-center gap-1 font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            Open {restaurant.openHours}
          </span>

          <span className="text-slate-400 font-medium">
            {restaurant.methods.join(" • ")}
          </span>

          <span className="ml-auto inline-flex items-center gap-1 text-[11px] font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
            <Clock className="w-3 h-3 text-slate-400" />
            {restaurant.preparingTime}
          </span>
        </div>
      </div>
    </div>
  );
};

