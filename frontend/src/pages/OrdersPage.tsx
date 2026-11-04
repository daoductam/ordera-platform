import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft } from "lucide-react";
import type { CustomerOrder } from "@/types/order";
import { MOCK_ORDERS } from "@/mock/orderMock";
import { ActiveOrdersView } from "@/components/orders/ActiveOrdersView";
import { OrderHistoryView } from "@/components/orders/OrderHistoryView";
import { EmptyOrdersView } from "@/components/orders/EmptyOrdersView";
import { OrderDetailModal } from "@/components/orders/OrderDetailModal";
import { EReceiptModal } from "@/components/orders/EReceiptModal";

export const OrdersPage: React.FC = () => {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<"active" | "history" | "empty">("active");
  const [selectedOrder, setSelectedOrder] = useState<CustomerOrder | null>(MOCK_ORDERS[1]); // #005 default for Figma 06.05
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [isReceiptModalOpen, setIsReceiptModalOpen] = useState(false);

  const activeOrders = MOCK_ORDERS.filter((o) => o.status === "preparing");
  const pastOrders = MOCK_ORDERS.filter((o) => o.status === "completed");

  const handleOpenDetails = (order: CustomerOrder) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(true);
  };

  const handleReorder = (_order: CustomerOrder) => {
    // Navigates to checkout / cart with order items
    navigate("/cart");
  };

  const handleGetReceipt = (order: CustomerOrder) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(false);
    setIsReceiptModalOpen(true);
  };

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center">
      <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl pb-24">
        {/* Header */}
        <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
          <button
            onClick={() => navigate("/account")}
            className="flex items-center gap-1.5 text-slate-900 hover:text-slate-700 active:scale-95 transition-all"
          >
            <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
            <span className="font-black text-sm tracking-tight">Order History</span>
          </button>
        </header>

        <div className="p-4">
          {/* TAB 1: ĐƠN ĐANG HOẠT ĐỘNG (Figma 06.02) */}
          {activeTab === "active" && (
            <ActiveOrdersView
              orders={activeOrders}
              onOpenDetails={handleOpenDetails}
              onReorder={handleReorder}
              onSwitchToHistory={() => setActiveTab("history")}
            />
          )}

          {/* TAB 2: LỊCH SỬ ĐƠN HÀNG (Figma 06.03) */}
          {activeTab === "history" && (
            <OrderHistoryView
              orders={pastOrders}
              onOpenDetails={handleOpenDetails}
              onReorder={handleReorder}
              onSwitchToActive={() => setActiveTab("active")}
            />
          )}

          {/* TAB 3: TRẠNG THÁI TRỐNG (Figma 06.04) */}
          {activeTab === "empty" && (
            <EmptyOrdersView
              onGoToShop={() => navigate("/")}
              onViewHistory={() => setActiveTab("history")}
            />
          )}
        </div>

        {/* MODAL CHI TIẾT ĐƠN HÀNG (Figma 06.05) */}
        <OrderDetailModal
          isOpen={isDetailModalOpen}
          order={selectedOrder}
          onClose={() => setIsDetailModalOpen(false)}
          onGetReceipt={handleGetReceipt}
          onReorder={handleReorder}
        />

        {/* MODAL GỬI BIÊN NHẬN ĐIỆN TỬ E-RECEIPT (Figma 06.08 - 06.10) */}
        <EReceiptModal
          isOpen={isReceiptModalOpen}
          order={selectedOrder}
          onClose={() => setIsReceiptModalOpen(false)}
        />
      </div>
    </div>
  );
};

