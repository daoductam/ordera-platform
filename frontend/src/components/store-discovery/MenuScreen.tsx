import React, { useState } from "react";
import {
  Menu,
  ChevronDown,
  Search,
  Flame,
  Plus,
  Check,
  Clock,
  Heart,
  Bike,
  User,
  ListFilter,
  Zap,
  Wheat,
  Leaf,
  MapPin,
  UtensilsCrossed,
} from "lucide-react";
import type { Restaurant, OrderOptions, MenuItem } from "@/types/store-discovery";
import { MOCK_MENU_ITEMS } from "@/mock/storeDiscoveryMock";
import { DrawerMenu } from "@/components/auth/DrawerMenu";
import { OrderOptionsModal } from "@/components/store-discovery/OrderOptionsModal";
import { useNavigate } from "react-router-dom";

export interface MenuScreenProps {
  restaurant: Restaurant;
  orderOptions: OrderOptions;
  onBack: () => void;
  onUpdateOrderOptions?: (options: OrderOptions) => void;
}

export const MenuScreen: React.FC<MenuScreenProps> = ({
  restaurant,
  orderOptions: initialOrderOptions,
  onBack,
  onUpdateOrderOptions,
}) => {
  const [orderOptions, setOrderOptions] = useState<OrderOptions>(initialOrderOptions);
  const [isOptionsModalOpen, setIsOptionsModalOpen] = useState(false);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("Popular");
  const [selectedSubCategory, setSelectedSubCategory] = useState("Fried maki");
  const [addedItemIds, setAddedItemIds] = useState<{ [id: string]: boolean }>({});
  const [searchQuery, setSearchQuery] = useState("");
  const [showSearch, setShowSearch] = useState(false);
  const [currentLang, setCurrentLang] = useState<{ code: string; flag: string; label: string }>({
    code: "EN",
    flag: "🇬🇧",
    label: "English",
  });
  const [showLangMenu, setShowLangMenu] = useState(false);
  const navigate = useNavigate();

  const categories = ["Popular", "Maki", "Nigiri", "Drinks", "Hot Food"];
  const subCategories = ["Fresh maki", "Fried maki", "Special roll", "Tempura", "Sashimi"];

  const languages = [
    { code: "EN", flag: "🇬🇧", label: "English" },
    { code: "NO", flag: "🇳🇴", label: "Norsk" },
    { code: "VI", flag: "🇻🇳", label: "Tiếng Việt" },
  ];

  const filteredItems = MOCK_MENU_ITEMS.filter((item) => {
    const matchesCat =
      selectedCategory === "Popular" || item.category === selectedCategory;
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  const handleAddItem = (item: MenuItem) => {
    setAddedItemIds((prev) => ({ ...prev, [item.id]: true }));
    setTimeout(() => {
      setAddedItemIds((prev) => ({ ...prev, [item.id]: false }));
    }, 1200);
  };

  const handleSaveOptions = (newOptions: OrderOptions) => {
    setOrderOptions(newOptions);
    if (onUpdateOrderOptions) onUpdateOrderOptions(newOptions);
  };

  return (
    <div className="bg-white min-h-screen pb-24 text-slate-900 font-sans">
      {/* 1. HERO BANNER WITH OVERLAID TOP APP BAR (Figma 01.06) */}
      <div className="relative w-full h-64 bg-slate-900">
        <img
          src={restaurant?.image || "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&q=80"}
          alt={restaurant?.name || "Burger Hero"}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-linear-to-b from-black/60 via-transparent to-black/30" />

        {/* TOP APP BAR (Floating directly over the hero image) */}
        <div className="absolute top-0 left-0 right-0 z-30 pt-3 px-3.5 flex items-center justify-between">
          {/* Nút 3 sọc (Hamburger Menu) */}
          <button
            onClick={() => setIsDrawerOpen(true)}
            title="Menu"
            className="w-9 h-9 rounded-full bg-slate-900/80 backdrop-blur-md hover:bg-slate-800 active:scale-95 flex items-center justify-center text-white transition-all shadow-md shrink-0 border border-white/10"
          >
            <Menu className="w-5 h-5 stroke-[2.5]" />
          </button>

          {/* Nút Chọn cách nhận (Delivery Pill Dropdown) */}
          <button
            onClick={() => setIsOptionsModalOpen(true)}
            className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-slate-900/85 backdrop-blur-md hover:bg-slate-800 text-white text-xs font-bold transition-all shadow-md shrink-0 border border-white/10"
          >
            <Bike className="w-3.5 h-3.5 text-amber-400" />
            <span>{orderOptions.method}</span>
            <ChevronDown className="w-3.5 h-3.5 text-slate-300" />
          </button>

          {/* Cụm công cụ bên phải: Search + Đổi ngôn ngữ + Profile */}
          <div className="flex items-center gap-1.5 shrink-0">
            {/* Nút Tìm kiếm */}
            <button
              onClick={() => setShowSearch(!showSearch)}
              title="Search"
              className="w-8 h-8 rounded-full bg-slate-900/80 backdrop-blur-md hover:bg-slate-800 active:scale-95 flex items-center justify-center text-white transition-all shadow-md border border-white/10"
            >
              <Search className="w-3.5 h-3.5" />
            </button>

            {/* Nút Đổi Ngôn Ngữ (UK Flag SVG chuẩn tròn không bị lỗi chữ GB) */}
            <div className="relative">
              <button
                onClick={() => setShowLangMenu(!showLangMenu)}
                title={currentLang.label}
                className="w-8 h-8 rounded-full bg-slate-900/80 backdrop-blur-md hover:bg-slate-800 active:scale-95 flex items-center justify-center transition-all shadow-md border border-white/10 overflow-hidden"
              >
                <div className="w-5 h-5 rounded-full overflow-hidden shrink-0 flex items-center justify-center">
                  <svg viewBox="0 0 60 30" className="w-full h-full object-cover">
                    <clipPath id="uk-flag-clip">
                      <path d="M0,0 v30 h60 v-30 z"/>
                    </clipPath>
                    <clipPath id="uk-flag-t">
                      <path d="M30,15 h30 v15 z v15 h-30 z h-30 v-15 z v-15 h30 z"/>
                    </clipPath>
                    <g clipPath="url(#uk-flag-clip)">
                      <path d="M0,0 v30 h60 v-30 z" fill="#012169"/>
                      <path d="M0,0 L60,30 M60,0 L0,30" stroke="#fff" strokeWidth="6"/>
                      <path d="M0,0 L60,30 M60,0 L0,30" clipPath="url(#uk-flag-t)" stroke="#C8102E" strokeWidth="4"/>
                      <path d="M30,0 v30 M0,15 h60" stroke="#fff" strokeWidth="10"/>
                      <path d="M30,0 v30 M0,15 h60" stroke="#C8102E" strokeWidth="6"/>
                    </g>
                  </svg>
                </div>
              </button>

              {/* Dropdown Menu Ngôn ngữ */}
              {showLangMenu && (
                <div className="absolute right-0 mt-2 w-32 bg-white rounded-2xl shadow-xl border border-slate-100 py-1.5 z-40 animate-in fade-in zoom-in-95">
                  {languages.map((l) => (
                    <button
                      key={l.code}
                      onClick={() => {
                        setCurrentLang(l);
                        setShowLangMenu(false);
                      }}
                      className={`w-full px-3 py-1.5 text-left text-xs flex items-center gap-2 hover:bg-slate-50 transition-colors ${
                        currentLang.code === l.code ? "font-bold text-amber-600 bg-amber-50/50" : "text-slate-700"
                      }`}
                    >
                      <span>{l.flag}</span>
                      <span>{l.label}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Nút Profile Thành Viên */}
            <button
              onClick={() => navigate("/account")}
              title="Account / Profile"
              className="w-8 h-8 rounded-full bg-slate-900/80 backdrop-blur-md hover:bg-slate-800 active:scale-95 flex items-center justify-center text-white transition-all shadow-md border border-white/10"
            >
              <User className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Nút Yêu Thích (Heart) */}
        <button className="absolute top-14 right-3.5 w-8 h-8 rounded-full bg-black/40 backdrop-blur-md flex items-center justify-center text-white hover:text-red-400 transition-colors border border-white/10">
          <Heart className={`w-4 h-4 ${restaurant?.isFavorite ? "fill-red-500 text-red-500" : ""}`} />
        </button>

        {/* Logo Cửa hàng (Linn Sushi badge bo góc trắng) */}
        <div className="absolute -bottom-6 left-4 w-18 h-18 rounded-2xl bg-white p-1.5 shadow-xl border border-slate-100 overflow-hidden flex items-center justify-center z-10">
          <div className="w-full h-full rounded-xl bg-orange-50 border border-orange-100 flex flex-col items-center justify-center p-1 text-center">
            <span className="text-[9px] font-black tracking-tighter text-amber-900 leading-none">
              🥢 LINN
            </span>
            <span className="text-[8px] font-bold text-red-600 leading-none mt-0.5">
              SUSHI
            </span>
          </div>
        </div>
      </div>

      {/* Ô Nhập Tìm Kiếm Khi Bấm Icon Search */}
      {showSearch && (
        <div className="p-3 bg-slate-50 border-b border-slate-200 animate-in slide-in-from-top-2">
          <input
            type="text"
            autoFocus
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search sushi, maki, burger, drinks..."
            className="w-full bg-white border border-slate-300 rounded-xl px-3.5 py-2 text-xs focus:outline-hidden focus:border-amber-500 font-medium text-slate-900 shadow-2xs"
          />
        </div>
      )}

      {/* 3. SHOP DETAILS SECTION (2 cột thông tin chi tiết chuẩn Figma 1.6) */}
      <div className="pt-8 px-4 pb-3 bg-white space-y-2 border-b border-slate-100">
        <h2 className="text-lg font-black text-slate-900">{restaurant?.name || "Shop Name 1"}</h2>

        <div className="grid grid-cols-2 gap-x-2 gap-y-1.5 text-[11px] text-slate-500 pt-0.5">
          {/* Cột trái */}
          <div className="flex items-center gap-1.5 truncate">
            <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate">{restaurant?.address || "Shop Address"}</span>
          </div>

          {/* Cột phải */}
          <div className="flex items-center gap-1.5 truncate">
            <UtensilsCrossed className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate">
              {restaurant?.methods ? restaurant.methods.join(" . ") : "Dine In . Delivery . Takeaway"}
            </span>
          </div>

          {/* Cột trái: Giờ mở cửa */}
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
            <span className="font-semibold text-emerald-700">
              {restaurant?.openHours ? `Open ${restaurant.openHours}` : "Open 10:30 AM - 10:00 PM"}
            </span>
          </div>

          {/* Cột phải: Thời gian chuẩn bị */}
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-amber-500 shrink-0" />
            <span>
              {restaurant?.preparingTime ? `Preparing time: ${restaurant.preparingTime}` : "Preparing time: 15 Mintues"}
            </span>
          </div>
        </div>
      </div>

      {/* 4. SUMMER SALES PROMO BANNER (Hồng pastel, chữ đỏ, nút Learn more vàng) */}
      <div className="px-4 py-3">
        <div className="bg-[#FFF1F2] border border-[#FFE4E6] rounded-2xl p-3.5 flex items-center justify-between gap-3 shadow-2xs">
          <div className="space-y-0.5 max-w-[70%]">
            <h3 className="font-black text-xs text-red-500 tracking-tight">
              Summer Sales: 20 % Off
            </h3>
            <p className="text-[10px] text-slate-500 leading-tight line-clamp-2">
              Lorum lorum lorum lorum lorum lorum lorum lorumlorum lorum lorum lorum lorum lorum
            </p>
          </div>
          <button className="px-3.5 py-1.5 bg-[#FBBF24] hover:bg-[#F59E0B] active:scale-95 text-slate-950 font-black rounded-full text-xs shrink-0 shadow-2xs transition-all">
            Learn more
          </button>
        </div>
      </div>

      {/* 5. LEVEL 1: CATEGORY TABS WITH ICON (Figma: List Icon + Underline tabs) */}
      <div className="px-4 border-b border-slate-200">
        <div className="flex items-center gap-4 overflow-x-auto no-scrollbar text-xs">
          {/* Icon danh mục bên trái */}
          <button className="text-slate-700 hover:text-slate-900 shrink-0 py-2">
            <ListFilter className="w-4 h-4 stroke-[2.5]" />
          </button>

          {/* Danh sách tab chính */}
          {categories.map((cat) => {
            const isSelected = selectedCategory === cat;
            return (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`py-2 whitespace-nowrap font-bold transition-all relative shrink-0 ${
                  isSelected
                    ? "text-amber-500 font-black border-b-2 border-amber-500"
                    : "text-slate-500 hover:text-slate-800"
                }`}
              >
                {cat}
              </button>
            );
          })}
        </div>
      </div>

      {/* 6. LEVEL 2: SUB-CATEGORY PILLS (Figma: Fresh maki, Fried maki active capsule, ...) */}
      <div className="px-4 py-2.5 overflow-x-auto no-scrollbar flex items-center gap-2">
        {subCategories.map((sub, idx) => {
          const isSelected = selectedSubCategory === sub;
          return (
            <button
              key={idx}
              onClick={() => setSelectedSubCategory(sub)}
              className={`whitespace-nowrap px-3.5 py-1.5 rounded-full text-xs font-bold transition-all shrink-0 ${
                isSelected
                  ? "bg-[#FBBF24] text-slate-950 font-black shadow-xs scale-102"
                  : "bg-white border border-slate-300 text-slate-600 hover:bg-slate-50"
              }`}
            >
              {sub}
            </button>
          );
        })}
      </div>

      {/* 7. SECTION HEADER (Figma: 🔥 Fried maki + Subtitle) */}
      <div className="px-4 pt-2 pb-1">
        <h3 className="font-black text-sm text-slate-900 flex items-center gap-1.5">
          <Flame className="w-4 h-4 text-orange-500 fill-orange-500" />
          <span>{selectedSubCategory}</span>
        </h3>
        <p className="text-[11px] text-slate-400 mt-0.5">
          {selectedSubCategory} description
        </p>
      </div>

      {/* 8. MENU ITEM CARDS (Laks Maki, Tuna Maki...) */}
      <div className="px-4 py-2 space-y-3">
        {filteredItems.map((item) => (
          <div
            key={item.id}
            className="bg-white rounded-2xl p-3 border border-slate-100 shadow-xs flex items-center justify-between gap-3 hover:border-slate-200 transition-all"
          >
            {/* Nội dung bên trái */}
            <div className="flex-1 min-w-0 space-y-1">
              <div className="flex items-center gap-2">
                <h4 className="font-extrabold text-sm text-slate-900 truncate">
                  {item.name}
                </h4>
              </div>

              {/* Giá tiền + Badge Popular màu tím */}
              <div className="flex items-center gap-2">
                <span
                  className={`font-black text-xs ${
                    item.originalPrice ? "text-red-500" : "text-slate-900"
                  }`}
                >
                  {item.price.toFixed(2).replace(".", ",")} NOK
                </span>
                {item.originalPrice && (
                  <span className="text-[11px] text-slate-400 line-through">
                    {item.originalPrice.toFixed(2).replace(".", ",")} NOK
                  </span>
                )}
                {item.isPopular && (
                  <span className="bg-[#7C3AED] text-white text-[9px] font-black px-1.5 py-0.5 rounded-md flex items-center gap-0.5">
                    <Zap className="w-2.5 h-2.5 fill-white" /> Popular
                  </span>
                )}
              </div>

              {/* Mô tả dài 2 dòng */}
              <p className="text-[11px] text-slate-500 leading-snug line-clamp-2">
                {item.description}
              </p>

              {/* Hàng Icons Dinh Dưỡng: Gluten Free, Organic, Spicy */}
              <div className="flex items-center gap-1.5 pt-1 text-[10px]">
                <span
                  title="Gluten Free"
                  className="w-5 h-5 rounded-full bg-slate-100 flex items-center justify-center text-slate-600"
                >
                  <Wheat className="w-3 h-3" />
                </span>
                <span
                  title="Organic"
                  className="w-5 h-5 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center"
                >
                  <Leaf className="w-3 h-3" />
                </span>
                <span
                  title="Spicy"
                  className="w-5 h-5 rounded-full bg-red-50 text-red-500 flex items-center justify-center font-bold text-[10px]"
                >
                  🌶️
                </span>
              </div>
            </div>

            {/* Ảnh Món Ăn Bo Góc + Nút Dấu Cộng Đen (Floating '+' button) */}
            <div className="relative w-20 h-20 rounded-2xl overflow-hidden shrink-0 bg-slate-100 shadow-2xs">
              <img
                src={item.image}
                alt={item.name}
                className="w-full h-full object-cover"
              />
              <button
                onClick={() => handleAddItem(item)}
                className={`absolute bottom-1 right-1 w-7 h-7 rounded-full flex items-center justify-center transition-all active:scale-90 shadow-md ${
                  addedItemIds[item.id]
                    ? "bg-emerald-600 text-white"
                    : "bg-slate-950 hover:bg-slate-800 text-white"
                }`}
              >
                {addedItemIds[item.id] ? (
                  <Check className="w-3.5 h-3.5 stroke-[3]" />
                ) : (
                  <Plus className="w-3.5 h-3.5 stroke-[3]" />
                )}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* MODAL ĐỔI CÁCH NHẬN (01.03 Change Order Options) */}
      <OrderOptionsModal
        isOpen={isOptionsModalOpen}
        onClose={() => setIsOptionsModalOpen(false)}
        currentOptions={orderOptions}
        onSave={handleSaveOptions}
      />

      {/* DRAWER MENU ĐƯỢC MỞ KHI BẤM NÚT 3 SỌC (02.01 Menu · Khách) */}
      <DrawerMenu
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        isLoggedIn={false}
        onGoToLogin={() => {
          setIsDrawerOpen(false);
          navigate("/auth");
        }}
        onGoToRegister={() => {
          setIsDrawerOpen(false);
          navigate("/auth");
        }}
        onGoToProfile={() => {
          setIsDrawerOpen(false);
          navigate("/account");
        }}
        onSeeAllShop={() => {
          setIsDrawerOpen(false);
          onBack();
        }}
      />
    </div>
  );
};
