import React, { useState } from "react";
import { X, MapPin, Utensils, ShoppingBag, Bike, Clock, Calendar } from "lucide-react";
import type { OrderOptions, OrderMethod, OrderTiming } from "@/types/store-discovery";

export interface OrderOptionsModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentOptions: OrderOptions;
  onSave: (options: OrderOptions) => void;
}

export const OrderOptionsModal: React.FC<OrderOptionsModalProps> = ({
  isOpen,
  onClose,
  currentOptions,
  onSave,
}) => {
  const [method, setMethod] = useState<OrderMethod>(currentOptions.method);
  const [timing, setTiming] = useState<OrderTiming>(currentOptions.timing);
  const [scheduleTime, setScheduleTime] = useState<string>("12:30 PM");

  if (!isOpen) return null;

  const handleDone = () => {
    onSave({
      ...currentOptions,
      method,
      timing,
      scheduledTime: timing === "schedule" ? scheduleTime : undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in">
      <div className="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 space-y-4 max-h-[90vh] overflow-y-auto shadow-2xl animate-in slide-in-from-bottom-5">
        {/* Header Modal */}
        <div className="flex items-center justify-between pb-2 border-b border-slate-100">
          <h3 className="font-extrabold text-base text-slate-900">Change Order Options</h3>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Location Display */}
        <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-3.5 flex items-start gap-3">
          <div className="w-8 h-8 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center shrink-0 mt-0.5">
            <MapPin className="w-4 h-4" />
          </div>
          <div>
            <span className="text-[10px] font-black uppercase text-slate-400 tracking-wider">
              Location
            </span>
            <h4 className="font-bold text-sm text-slate-900">{currentOptions.locationName}</h4>
            <p className="text-xs text-slate-500 mt-0.5">{currentOptions.locationAddress}</p>
          </div>
        </div>

        {/* Segmented Buttons: Dine In, Takeaway, Delivery */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold text-slate-700">Order Method</label>
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: "Dine In" as OrderMethod, label: "Dine In", icon: Utensils },
              { id: "Takeaway" as OrderMethod, label: "Takeaway", icon: ShoppingBag },
              { id: "Delivery" as OrderMethod, label: "Delivery", icon: Bike },
            ].map((item) => {
              const isSelected = method === item.id;
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => setMethod(item.id)}
                  className={`py-3 px-2 rounded-2xl border text-xs font-bold flex flex-col items-center justify-center gap-1.5 transition-all ${
                    isSelected
                      ? "bg-amber-500 text-white border-amber-500 shadow-md shadow-amber-500/20 scale-[1.02]"
                      : "bg-white border-slate-200 text-slate-700 hover:bg-slate-50"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Timing Options */}
        <div className="space-y-2 pt-1">
          <label className="text-xs font-bold text-slate-900 block">
            When would you like to have the order?
          </label>

          {/* Option 1: As Soon As Possible */}
          <div
            onClick={() => setTiming("asap")}
            className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
              timing === "asap"
                ? "bg-amber-50/60 border-amber-400 text-slate-900"
                : "bg-white border-slate-200 text-slate-600 hover:bg-slate-50"
            }`}
          >
            <div className="flex items-center gap-3">
              <div
                className={`w-4 h-4 rounded-full border-2 flex items-center justify-center ${
                  timing === "asap" ? "border-amber-500 bg-amber-500" : "border-slate-300"
                }`}
              >
                {timing === "asap" && <div className="w-1.5 h-1.5 rounded-full bg-white" />}
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5 text-amber-600" /> As soon as possible
                </p>
                <p className="text-[11px] text-slate-500">Preparing time: 10-15 Minutes</p>
              </div>
            </div>
          </div>

          {/* Option 2: Schedule */}
          <div
            onClick={() => setTiming("schedule")}
            className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex flex-col gap-2 ${
              timing === "schedule"
                ? "bg-amber-50/60 border-amber-400 text-slate-900"
                : "bg-white border-slate-200 text-slate-600 hover:bg-slate-50"
            }`}
          >
            <div className="flex items-center gap-3">
              <div
                className={`w-4 h-4 rounded-full border-2 flex items-center justify-center ${
                  timing === "schedule" ? "border-amber-500 bg-amber-500" : "border-slate-300"
                }`}
              >
                {timing === "schedule" && <div className="w-1.5 h-1.5 rounded-full bg-white" />}
              </div>
              <div className="flex-1">
                <p className="text-xs font-bold text-slate-900 flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-slate-600" /> Schedule
                </p>
                <p className="text-[11px] text-slate-500">Select a convenient pick-up time</p>
              </div>
            </div>

            {timing === "schedule" && (
              <div className="pl-7 pt-1">
                <input
                  type="time"
                  value={scheduleTime}
                  onChange={(e) => setScheduleTime(e.target.value)}
                  className="bg-white border border-slate-200 rounded-xl px-3 py-1.5 text-xs font-bold text-slate-800"
                />
              </div>
            )}
          </div>
        </div>

        {/* Done Button */}
        <div className="pt-2">
          <button
            onClick={handleDone}
            className="w-full py-3.5 bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-900 rounded-2xl font-black text-sm shadow-md transition-all"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};

