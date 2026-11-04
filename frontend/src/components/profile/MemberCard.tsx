import React from "react";
import type { MemberTierName } from "@/types/profile";

export interface MemberCardProps {
  tier: MemberTierName;
  currentPoints: number;
  userName?: string;
  className?: string;
  onClick?: () => void;
}

export const MemberCard: React.FC<MemberCardProps> = ({
  tier,
  currentPoints,
  className = "",
  onClick,
}) => {
  // Bảng màu chuẩn 100% theo ảnh thiết kế Figma (04.10 - 04.15)
  const tierThemes: Record<
    MemberTierName,
    {
      bgColor: string;
      displayName: string;
    }
  > = {
    New: {
      bgColor: "bg-[#1EB759]", // Xanh lá cây (04.10 Hạng New)
      displayName: "New",
    },
    Silver: {
      bgColor: "bg-[#00A3FF]", // Xanh da trời cyan (04.11 Hạng Silver)
      displayName: "Silver",
    },
    Gold: {
      bgColor: "bg-[#F5B800]", // Vàng kim mustard (04.12 Hạng Gold)
      displayName: "Gold",
    },
    Platinum: {
      bgColor: "bg-[#FF5B4D]", // Đỏ cam san hô / Coral Red (04.13 Hạng Platinum)
      displayName: "Platinum",
    },
    Diamond: {
      bgColor: "bg-[#7C3AED]", // Tím hoa cà đậm / Electric Violet (04.14 Hạng Dimond)
      displayName: "Dimond",
    },
    Ruby: {
      bgColor: "bg-[#D9148E]", // Hồng cánh sen đậm / Vivid Magenta (04.15 Hạng Ruby)
      displayName: "Ruby",
    },
  };

  const theme = tierThemes[tier] || tierThemes.New;

  return (
    <div
      onClick={onClick}
      className={`relative w-full h-44 rounded-2xl p-5 text-white overflow-hidden shadow-lg ${theme.bgColor} transition-all duration-300 active:scale-[0.99] cursor-pointer select-none ${className}`}
    >
      {/* 2 Đường cong tròn mờ tạo chiều sâu chuẩn Figma 04.10 - 04.15 */}
      <div className="absolute -right-8 -bottom-16 w-52 h-52 rounded-full bg-white/15 pointer-events-none" />
      <div className="absolute right-8 -top-12 w-44 h-44 rounded-full bg-white/10 pointer-events-none" />

      <div className="relative z-10 flex flex-col justify-between h-full">
        {/* Top left: Nhãn AGENCYNAME MEMBER & Tên hạng */}
        <div>
          <span className="text-[10px] font-bold tracking-wider uppercase text-white/90">
            AGENCYNAME MEMBER
          </span>
          <h2 className="text-3xl font-extrabold tracking-tight mt-1 text-white">
            {theme.displayName}
          </h2>
        </div>

        {/* Bottom right: Current Point & Điểm */}
        <div className="text-right self-end">
          <span className="text-[10px] font-medium text-white/90 block">
            Current Point
          </span>
          <span className="text-3xl font-extrabold tracking-tight leading-none block mt-1 text-white">
            {currentPoints.toLocaleString()}
          </span>
        </div>
      </div>
    </div>
  );
};
