import React, { useState } from "react";
import { Search, Plus, Sparkles, Flame, Check } from "lucide-react";
import { MOCK_PRODUCTS } from "@/mock/mockData";
import { formatCurrency } from "@/lib/utils";
import { useCart } from "@/context/CartContext";
import type { ProductDto } from "@/types";

export const HomePage: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState("Tất cả");
  const [searchQuery, setSearchQuery] = useState("");
  const [addedId, setAddedId] = useState<string | null>(null);
  const { addItem } = useCart();

  const categories = [
    "Tất cả",
    "Trà Sữa Đậm Vị",
    "Trà Trái Cây",
    "Cà Phê Thủ Công",
    "Bánh Ngọt & Tráng Miệng",
  ];

  const filteredProducts = MOCK_PRODUCTS.filter((product) => {
    const matchCat =
      selectedCategory === "Tất cả" || product.category === selectedCategory;
    const matchSearch = product.name
      .toLowerCase()
      .includes(searchQuery.toLowerCase());
    return matchCat && matchSearch;
  });

  const handleAddToCart = (product: ProductDto) => {
    addItem(product);
    setAddedId(product.id);
    setTimeout(() => setAddedId(null), 1000);
  };

  return (
    <div className="p-4 space-y-4">
      {/* Banner Khuyến Mãi */}
      <div className="relative overflow-hidden rounded-2xl bg-linear-to-r from-orange-600 via-amber-600 to-yellow-500 p-4 text-white shadow-md">
        <div className="relative z-10 max-w-[70%]">
          <span className="inline-flex items-center gap-1 bg-white/20 backdrop-blur-xs text-[10px] font-black uppercase px-2 py-0.5 rounded-full mb-1">
            <Sparkles className="w-3 h-3" /> Thứ Tư Vàng
          </span>
          <h2 className="text-lg font-black leading-tight">
            Nhân đôi điểm (x2) mọi đơn hàng hôm nay!
          </h2>
          <p className="text-xs text-orange-100 mt-1">
            Áp dụng cho toàn bộ thành viên từ New đến Ruby VIP.
          </p>
        </div>
        <div className="absolute right-2 -bottom-2 w-28 h-28 opacity-25 font-black text-8xl select-none">
          %
        </div>
      </div>

      {/* Thanh Tìm Kiếm */}
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Tìm món ngon, cà phê, trà sữa..."
          className="w-full bg-white border border-slate-200/80 rounded-xl pl-10 pr-4 py-2.5 text-xs text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-all shadow-2xs"
        />
      </div>

      {/* Danh Mục Trượt Ngang (01.06 Thực đơn - Danh mục) */}
      <div className="flex gap-2 overflow-x-auto no-scrollbar py-1">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            className={`whitespace-nowrap px-3.5 py-2 rounded-full text-xs font-bold transition-all shrink-0 ${
              selectedCategory === cat
                ? "bg-slate-900 text-white shadow-xs"
                : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-100"
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Danh Sách Món Ăn (Grid) */}
      <div className="space-y-3 pt-1">
        <div className="flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-slate-900">
            {selectedCategory === "Tất cả" ? "Món Nổi Bật Hôm Nay" : selectedCategory}
          </h3>
          <span className="text-xs text-slate-400 font-medium">
            {filteredProducts.length} món
          </span>
        </div>

        <div className="grid grid-cols-1 gap-3">
          {filteredProducts.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-2xl p-3 border border-slate-100 shadow-xs flex gap-3 items-center hover:border-orange-200 transition-all"
            >
              <div className="relative w-20 h-20 rounded-xl overflow-hidden shrink-0 bg-slate-100">
                <img
                  src={product.image}
                  alt={product.name}
                  className="w-full h-full object-cover"
                />
                {product.isPopular && (
                  <span className="absolute top-1 left-1 bg-red-500 text-white text-[9px] font-black px-1.5 py-0.5 rounded-full flex items-center gap-0.5">
                    <Flame className="w-2.5 h-2.5 fill-white" /> HOT
                  </span>
                )}
              </div>

              <div className="flex-1 min-w-0">
                <h4 className="font-bold text-sm text-slate-900 truncate">
                  {product.name}
                </h4>
                <p className="text-[11px] text-slate-500 line-clamp-1 mt-0.5">
                  {product.description}
                </p>
                <div className="flex items-center gap-2 mt-2">
                  <span className="font-black text-sm text-orange-600">
                    {formatCurrency(product.price)}
                  </span>
                  {product.originalPrice && (
                    <span className="text-[11px] text-slate-400 line-through">
                      {formatCurrency(product.originalPrice)}
                    </span>
                  )}
                </div>
              </div>

              {/* Nút Thêm Món */}
              <button
                onClick={() => handleAddToCart(product)}
                className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 transition-all active:scale-90 ${
                  addedId === product.id
                    ? "bg-emerald-600 text-white"
                    : "bg-orange-50 text-orange-600 hover:bg-orange-500 hover:text-white"
                }`}
              >
                {addedId === product.id ? (
                  <Check className="w-5 h-5 stroke-[2.5]" />
                ) : (
                  <Plus className="w-5 h-5 stroke-[2.5]" />
                )}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

