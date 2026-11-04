import React from "react";
import { Award, Sparkles } from "lucide-react";

export interface EmptyRewardsViewProps {
  type: "myRewards" | "getMore";
  onAction: () => void;
}

export const EmptyRewardsView: React.FC<EmptyRewardsViewProps> = ({
  type,
  onAction,
}) => {
  const isMyRewards = type === "myRewards";

  return (
    <div className="flex flex-col items-center justify-center py-24 px-4 text-center font-sans space-y-6">
      {/* Icon huy chương ngôi sao kèm tia sáng chuẩn Figma 07.05 & 07.06 */}
      <div className="relative">
        <div className="w-20 h-20 rounded-full bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-300">
          <Award className="w-10 h-10 stroke-[1.2]" />
        </div>
        {/* Sparkle dots xung quanh */}
        <div className="absolute -top-1 -right-2 text-slate-300">
          <Sparkles className="w-4 h-4" />
        </div>
        <div className="absolute -bottom-1 -left-2 text-slate-300">
          <Sparkles className="w-3.5 h-3.5" />
        </div>
      </div>

      {/* Dòng text thông báo */}
      <p className="text-xs font-medium text-slate-500">
        {isMyRewards
          ? "You have no rewards"
          : "No available rewards to redeem now"}
      </p>

      {/* Nút hành động dạng hộp chữ nhật viền xám mỏng chuẩn Figma 07.05 & 07.06 */}
      <div className="pt-2">
        <button
          onClick={onAction}
          className="px-8 py-2.5 rounded-lg border border-slate-300 bg-white text-slate-800 font-semibold text-xs shadow-2xs hover:bg-slate-50 active:scale-95 transition-all cursor-pointer"
        >
          {isMyRewards ? "Get More Rewards" : "Go back to Shop"}
        </button>
      </div>
    </div>
  );
};
