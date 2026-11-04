import React from "react";

export interface VoucherTicketIconProps {
  type: "fixed" | "percent";
  className?: string;
}

export const VoucherTicketIcon: React.FC<VoucherTicketIconProps> = ({
  type,
  className = "",
}) => {
  return (
    <div className={`shrink-0 flex items-center justify-center select-none ${className}`}>
      <svg
        width="54"
        height="36"
        viewBox="0 0 54 36"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="drop-shadow-2xs"
      >
        <path
          d="M 8 1.5 H 46 Q 52.5 1.5 52.5 8 V 13 A 5 5 0 0 0 52.5 23 V 28 Q 52.5 34.5 46 34.5 H 8 Q 1.5 34.5 1.5 28 V 23 A 5 5 0 0 0 1.5 13 V 8 Q 1.5 1.5 8 1.5 Z"
          fill="#FFF9E6"
          stroke="#F5B800"
          strokeWidth="2"
        />
        <text
          x="27"
          y="23.5"
          textAnchor="middle"
          fill="#D97706"
          fontSize="16"
          fontWeight="900"
          fontFamily="system-ui, -apple-system, sans-serif"
        >
          {type === "fixed" ? "$" : "%"}
        </text>
      </svg>
    </div>
  );
};
