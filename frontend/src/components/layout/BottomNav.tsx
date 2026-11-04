import React from "react";
import { Link, useLocation } from "react-router-dom";
import { User } from "lucide-react";

export interface BottomNavProps {
  brandLabel?: string;
}

export const BottomNav: React.FC<BottomNavProps> = ({ brandLabel = "Agency Name" }) => {
  const location = useLocation();

  // Ẩn thanh bottom nav CHỈ khi ở màn hình Kiosk toàn màn hình
  if (location.pathname === "/kiosk") {
    return null;
  }

  // Trang nào đang active "Shop / Agency"
  const isShopActive =
    location.pathname === "/" ||
    location.pathname === "/stores" ||
    location.pathname === "/drinks";

  // Trang nào đang active "Account"
  const isAccountActive =
    location.pathname === "/account" ||
    location.pathname === "/auth" ||
    location.pathname === "/profile" ||
    location.pathname === "/orders" ||
    location.pathname === "/history" ||
    location.pathname === "/rewards" ||
    location.pathname === "/tier" ||
    location.pathname === "/cart" ||
    location.pathname === "/checkout";

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-100 shadow-[0_-4px_16px_rgba(0,0,0,0.05)]">
      <div className="max-w-md mx-auto h-16 flex items-center justify-around px-8">
        {/* Nút 1: Shop Brand / Agency Name (Figma Frame 2608455) */}
        <Link
          to="/"
          className="flex flex-col items-center justify-center gap-1 group py-1 select-none min-w-[72px]"
        >
          {/* Square grey icon bo tròn nhẹ chuẩn Figma Frame 2608454 */}
          <div
            className={`w-6 h-6 rounded-[6px] transition-all ${
              isShopActive
                ? "bg-[#E5A800]"
                : "bg-[#D4D5D9] group-hover:bg-slate-400"
            }`}
          />
          <span
            className={`text-[11px] tracking-tight transition-colors ${
              isShopActive ? "font-bold text-[#E5A800]" : "font-medium text-slate-600"
            }`}
          >
            {brandLabel}
          </span>
        </Link>

        {/* Nút 2: Account (Figma Frame 2608456) */}
        <Link
          to="/account"
          className="flex flex-col items-center justify-center gap-1 group py-1 select-none min-w-[72px]"
        >
          {/* Vòng tròn vàng có icon user màu trắng chuẩn Figma */}
          <div
            className={`w-6 h-6 rounded-full flex items-center justify-center transition-all ${
              isAccountActive
                ? "bg-[#E5A800]"
                : "bg-[#D4D5D9] group-hover:bg-slate-400 text-white"
            }`}
          >
            <User className="w-3.5 h-3.5 text-white stroke-[2.5]" />
          </div>
          <span
            className={`text-[11px] tracking-tight transition-colors ${
              isAccountActive ? "font-bold text-[#E5A800]" : "font-medium text-slate-600"
            }`}
          >
            Account
          </span>
        </Link>
      </div>
    </nav>
  );
};
