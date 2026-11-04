import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import type { CheckoutDetails } from "@/types/checkout";
import { DEFAULT_CHECKOUT_DETAILS } from "@/mock/checkoutMock";
import { ReorderCartView } from "@/components/cart/ReorderCartView";
import { CheckoutView } from "@/components/checkout/CheckoutView";
import { EReceiptModal } from "@/components/orders/EReceiptModal";
import type { CustomerOrder } from "@/types/order";

export const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Kiểm tra xem đang ở màn hình Checkout (06.07) hay Giỏ hàng (06.06)
  const isCheckout = location.pathname === "/checkout";

  const [checkoutDetails, setCheckoutDetails] = useState<CheckoutDetails>(
    DEFAULT_CHECKOUT_DETAILS
  );
  const [completedOrder, setCompletedOrder] = useState<CustomerOrder | null>(
    null
  );
  const [isReceiptModalOpen, setIsReceiptModalOpen] = useState(false);

  const handlePaySuccess = (finalDetails: CheckoutDetails) => {
    const newOrder: CustomerOrder = {
      id: "order-" + Date.now(),
      orderNumber: "#005",
      customerName: finalDetails.customerName || "Naomi - #005",
      restaurantName: finalDetails.storeName,
      placedAt: "Vừa xong",
      status: "preparing",
      pickupMethod:
        finalDetails.pickupMethod === "Counter" ? "Takeaway" : "Dine In",
      tableNumber: finalDetails.tableNumber,
      items: finalDetails.items,
      subtotal: finalDetails.subtotal,
      tipAmount: finalDetails.tipAmount,
      discountAmount: finalDetails.discountAmount,
      appliedVoucherCode: finalDetails.appliedVoucher?.code,
      appliedGiftName: finalDetails.appliedGift?.title,
      totalAmount: finalDetails.total,
      earnedPoints: finalDetails.pointsToEarn,
      eReceiptEmail: "henrytran.bi@gmail.com",
    };
    setCompletedOrder(newOrder);
    setIsReceiptModalOpen(true);
  };

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center">
      <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl">
        {/* STAGE 1: FIGMA 06.06 GIỎ HÀNG · ĐẶT LẠI */}
        {!isCheckout && (
          <ReorderCartView
            onBack={() => navigate(-1)}
            onProceedToCheckout={(cartData) => {
              setCheckoutDetails((prev) => ({
                ...prev,
                subtotal: cartData.subtotal,
                total: cartData.subtotal + prev.tipAmount - prev.discountAmount,
              }));
              navigate("/checkout");
            }}
          />
        )}

        {/* STAGE 2: FIGMA 06.07 CHECKOUT CƠ BẢN */}
        {isCheckout && (
          <CheckoutView
            initialDetails={checkoutDetails}
            onBack={() => navigate("/cart")}
            onPaySuccess={handlePaySuccess}
          />
        )}

        {/* MODAL GỬI BIÊN NHẬN ĐIỆN TỬ SAU KHI PAY (FIGMA 06.08 - 06.10) */}
        <EReceiptModal
          isOpen={isReceiptModalOpen}
          order={completedOrder}
          onClose={() => {
            setIsReceiptModalOpen(false);
            navigate("/orders");
          }}
        />
      </div>
    </div>
  );
};
