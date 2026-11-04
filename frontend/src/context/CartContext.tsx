import React, { createContext, useContext, useState, useEffect } from "react";
import type { CartItemDto, ProductDto, VoucherDto } from "@/types";

interface CartContextType {
  items: CartItemDto[];
  addItem: (product: ProductDto) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, delta: number) => void;
  clearCart: () => void;
  totalItems: number;
  subtotal: number;
  appliedVoucher: VoucherDto | null;
  applyVoucher: (voucher: VoucherDto) => void;
  removeVoucher: () => void;
  discountAmount: number;
  finalTotal: number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [items, setItems] = useState<CartItemDto[]>(() => {
    const saved = localStorage.getItem("ordera_cart");
    return saved ? JSON.parse(saved) : [];
  });
  const [appliedVoucher, setAppliedVoucher] = useState<VoucherDto | null>(null);

  useEffect(() => {
    localStorage.setItem("ordera_cart", JSON.stringify(items));
  }, [items]);

  const addItem = (product: ProductDto) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.productId === product.id);
      if (existing) {
        return prev.map((i) =>
          i.productId === product.id ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [
        ...prev,
        {
          productId: product.id,
          productName: product.name,
          price: product.price,
          quantity: 1,
          image: product.image,
        },
      ];
    });
  };

  const removeItem = (productId: string) => {
    setItems((prev) => prev.filter((i) => i.productId !== productId));
  };

  const updateQuantity = (productId: string, delta: number) => {
    setItems((prev) =>
      prev
        .map((i) => {
          if (i.productId === productId) {
            const newQ = i.quantity + delta;
            return newQ > 0 ? { ...i, quantity: newQ } : null;
          }
          return i;
        })
        .filter(Boolean) as CartItemDto[]
    );
  };

  const clearCart = () => {
    setItems([]);
    setAppliedVoucher(null);
  };

  const applyVoucher = (voucher: VoucherDto) => {
    setAppliedVoucher(voucher);
  };

  const removeVoucher = () => {
    setAppliedVoucher(null);
  };

  const totalItems = items.reduce((sum, i) => sum + i.quantity, 0);
  const subtotal = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  let discountAmount = 0;
  if (appliedVoucher) {
    if (subtotal >= appliedVoucher.minOrderValue) {
      discountAmount = appliedVoucher.discountValue;
    }
  }

  const finalTotal = Math.max(0, subtotal - discountAmount);

  return (
    <CartContext.Provider
      value={{
        items,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        totalItems,
        subtotal,
        appliedVoucher,
        applyVoucher,
        removeVoucher,
        discountAmount,
        finalTotal,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within CartProvider");
  }
  return context;
};

