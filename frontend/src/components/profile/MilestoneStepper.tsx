import React from "react";
import type { MemberTierName } from "@/types/profile";

export interface MilestoneStepperProps {
  currentTier: MemberTierName;
  lifetimePoints: number;
  nextTierPointsNeeded: number;
  onSelectTier?: (tier: MemberTierName) => void;
}

export const MilestoneStepper: React.FC<MilestoneStepperProps> = ({
  currentTier,
  lifetimePoints,
  nextTierPointsNeeded,
  onSelectTier,
}) => {
  // 6 Cột mốc và mã màu chính xác theo Figma 04.10 - 04.15
  const steps: {
    tier: MemberTierName;
    code: string;
    points: number;
    hexColor: string;
  }[] = [
    { tier: "New", code: "N", points: 0, hexColor: "#1EB759" }, // Xanh lá
    { tier: "Silver", code: "S", points: 200, hexColor: "#00A3FF" }, // Xanh da trời
    { tier: "Gold", code: "G", points: 400, hexColor: "#F5B800" }, // Vàng kim
    { tier: "Platinum", code: "P", points: 1000, hexColor: "#FF5B4D" }, // Đỏ san hô
    { tier: "Diamond", code: "D", points: 2000, hexColor: "#7C3AED" }, // Tím
    { tier: "Ruby", code: "R", points: 4000, hexColor: "#D9148E" }, // Hồng cánh sen
  ];

  // Tính toán vị trí tiến trình chính xác giữa các node
  const calculateProgressPercent = (pts: number) => {
    if (pts <= 0) return 0;
    if (pts < 200) return (pts / 200) * 20;
    if (pts < 400) return 20 + ((pts - 200) / 200) * 20;
    if (pts < 1000) return 40 + ((pts - 400) / 600) * 20;
    if (pts < 2000) return 60 + ((pts - 1000) / 1000) * 20;
    if (pts < 4000) return 80 + ((pts - 2000) / 2000) * 20;
    return 100;
  };

  const progressPercent = calculateProgressPercent(lifetimePoints);

  return (
    <div className="w-full bg-white py-2 space-y-2.5">
      {/* 1. Lifetime Points & Tiêu đề cần bao nhiêu điểm để lên hạng */}
      <div className="space-y-0.5">
        <div className="text-xs text-slate-800 font-semibold">
          Lifetime Points:{" "}
          <span className="font-bold text-slate-900">{lifetimePoints} points</span>
        </div>
        <p className="text-[11px] text-slate-500 font-medium">
          {nextTierPointsNeeded > 0
            ? `You need ${nextTierPointsNeeded} points to go to the next level of membership`
            : "You're at the top – enjoy all the exclusive benefits!"}
        </p>
      </div>

      {/* 2. Track tiến trình nối 6 node N - S - G - P - D - R */}
      <div className="relative pt-2 pb-1">
        {/* Đường ray nền màu xám nhạt */}
        <div className="absolute top-4 left-3 right-3 h-[1.5px] bg-slate-200 -translate-y-1/2" />

        {/* Đường ray đã hoàn thành màu vàng cam hổ phách */}
        {progressPercent > 0 && (
          <div
            className="absolute top-4 left-3 h-[1.5px] bg-[#E5A800] -translate-y-1/2 transition-all duration-500"
            style={{ width: `calc(${progressPercent}% - 6px)` }}
          />
        )}

        {/* Chấm tròn nhỏ thể hiện vị trí điểm hiện tại trên thanh ray */}
        {progressPercent > 0 && progressPercent < 100 && (
          <div
            className="absolute top-4 -translate-y-1/2 w-2 h-2 rounded-full bg-[#E5A800] ring-1 ring-white shadow-2xs z-20 pointer-events-none transition-all duration-500"
            style={{ left: `calc(${progressPercent}% - 4px)` }}
          />
        )}

        {/* 6 Cột mốc nút tròn N - S - G - P - D - R */}
        <div className="relative flex items-center justify-between z-10">
          {steps.map((step) => {
            const isCurrent = currentTier === step.tier;

            return (
              <button
                key={step.code}
                onClick={() => onSelectTier?.(step.tier)}
                title={`Hạng ${step.tier} (${step.points} pts)`}
                className="flex flex-col items-center group focus:outline-hidden cursor-pointer active:scale-95 transition-transform"
              >
                {/* Node hình tròn mang màu sắc chuẩn của từng hạng */}
                <div
                  className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold text-white shadow-2xs transition-all ${
                    isCurrent
                      ? "ring-2 ring-amber-400 ring-offset-2 ring-offset-white scale-110"
                      : "hover:scale-105"
                  }`}
                  style={{ backgroundColor: step.hexColor }}
                >
                  {step.code}
                </div>

                {/* Điểm số mốc bên dưới */}
                <span className="text-[10px] mt-1 font-medium text-slate-400 group-hover:text-slate-700 transition-colors">
                  {step.points}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
