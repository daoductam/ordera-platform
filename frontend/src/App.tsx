import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { CartProvider } from "@/context/CartContext";
import { MobileContainer } from "@/components/layout/MobileContainer";
import { StoreDiscoveryPage } from "@/pages/StoreDiscoveryPage";
import { HomePage } from "@/pages/HomePage";
import { TierPage } from "@/pages/TierPage";
import { RewardsPage } from "@/pages/RewardsPage";
import { CartPage } from "@/pages/CartPage";
import { KioskPage } from "@/pages/KioskPage";
import { AuthFlowPage } from "@/pages/AuthFlowPage";
import { AccountPage } from "@/pages/AccountPage";
import { OrdersPage } from "@/pages/OrdersPage";

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <CartProvider>
        <MobileContainer>
          <Routes>
            {/* Nhóm 01: Khám phá & Chọn cửa hàng (Figma 01.01 - 01.09) */}
            <Route path="/" element={<StoreDiscoveryPage />} />
            <Route path="/stores" element={<StoreDiscoveryPage />} />

            {/* Nhóm 02 & 03: Đăng nhập & Đăng ký xác thực thành viên (Figma 02.01 - 03.10) */}
            <Route path="/auth" element={<AuthFlowPage />} />

            {/* Nhóm 04 & 05: Hồ sơ, 6 Hạng thành viên & Thông báo hỗ trợ điểm (Figma 04.01 - 05.03) */}
            <Route path="/account" element={<AccountPage />} />
            <Route path="/profile" element={<AccountPage />} />

            {/* Nhóm 06: Đơn hàng, Giỏ hàng & Biên nhận điện tử E-Receipt (Figma 06.01 - 06.10) */}
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/history" element={<OrdersPage />} />

            {/* Nhóm 07: Ưu đãi & Đổi thưởng (Figma 07.01 - 07.08) */}
            <Route path="/rewards" element={<RewardsPage />} />

            {/* Nhóm 08: Áp mã & Quà trong Checkout (Figma 08.01 - 08.12) */}
            <Route path="/cart" element={<CartPage />} />
            <Route path="/checkout" element={<CartPage />} />

            {/* Loyalty tier & points ledger */}
            <Route path="/tier" element={<TierPage />} />

            {/* Menu đặt đồ uống & bánh */}
            <Route path="/drinks" element={<HomePage />} />

            {/* Chế độ Kiosk màn hình chạm tại quầy */}
            <Route path="/kiosk" element={<KioskPage />} />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </MobileContainer>
      </CartProvider>
    </BrowserRouter>
  );
};

export default App;
