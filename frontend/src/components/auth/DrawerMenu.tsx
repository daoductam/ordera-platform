import React from "react";
import { X, Store, User, ChevronRight, Info, Settings, HelpCircle, FileText, Smile } from "lucide-react";

export interface DrawerMenuProps {
  isOpen: boolean;
  onClose: () => void;
  isLoggedIn: boolean;
  onGoToLogin: () => void;
  onGoToRegister: () => void;
  onGoToProfile: () => void;
  onSeeAllShop: () => void;
}

export const DrawerMenu: React.FC<DrawerMenuProps> = ({
  isOpen,
  onClose,
  isLoggedIn,
  onGoToLogin,
  onGoToRegister,
  onGoToProfile,
  onSeeAllShop,
}) => {
  if (!isOpen) return null;

  const menuItems = [
    { label: "Shop Information", icon: Info },
    { label: "Setting", icon: Settings },
    { label: "Help", icon: HelpCircle },
    { label: "Terms & Policies", icon: FileText },
  ];

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex justify-start animate-in fade-in">
      <div className="bg-white w-full max-w-[340px] h-full flex flex-col justify-between p-6 shadow-2xl animate-in slide-in-from-left duration-300">
        <div className="space-y-6">
          {/* Close button */}
          <div className="flex justify-end">
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Brand Header (Figma: Welcome to Linn Sushi) */}
          <div className="text-center space-y-2">
            <div className="w-16 h-16 rounded-full bg-amber-500/10 border-2 border-amber-500/20 mx-auto flex items-center justify-center text-2xl shadow-inner">
              🍣
            </div>
            <div>
              <h3 className="font-black text-lg text-slate-900">Welcome to Linn Sushi</h3>
              <p className="text-xs text-slate-500 mt-0.5">Where fresh sushi meets your taste</p>
            </div>
          </div>

          {/* Primary Action Buttons */}
          <div className="space-y-2.5 pt-2">
            {/* Button 1: See All Shop */}
            <button
              onClick={onSeeAllShop}
              className="w-full py-3 px-4 rounded-2xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-800 text-xs font-bold flex items-center justify-center gap-2 transition-all shadow-2xs"
            >
              <Store className="w-4 h-4 text-slate-600" />
              <span>See All Shop</span>
            </button>

            {/* Button 2: Login or My Profile */}
            {isLoggedIn ? (
              <button
                onClick={onGoToProfile}
                className="w-full py-3 px-4 rounded-2xl bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-950 text-xs font-black flex items-center justify-center gap-2 transition-all shadow-md shadow-amber-400/20"
              >
                <User className="w-4 h-4" />
                <span>My Profile</span>
              </button>
            ) : (
              <button
                onClick={onGoToLogin}
                className="w-full py-3 px-4 rounded-2xl bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-950 text-xs font-black flex items-center justify-center gap-2 transition-all shadow-md shadow-amber-400/20"
              >
                <User className="w-4 h-4" />
                <span>Login</span>
              </button>
            )}

            {!isLoggedIn && (
              <p className="text-center text-xs text-slate-500 pt-1">
                Don't have account?{" "}
                <button
                  onClick={onGoToRegister}
                  className="font-bold text-amber-600 hover:underline"
                >
                  Sign up
                </button>
              </p>
            )}
          </div>

          {/* Menu Items List */}
          <div className="pt-4 border-t border-slate-100 divide-y divide-slate-100 text-xs">
            {menuItems.map((item, idx) => {
              const Icon = item.icon;
              return (
                <button
                  key={idx}
                  onClick={onClose}
                  className="w-full py-3.5 flex items-center justify-between text-slate-700 hover:text-slate-900 group transition-colors"
                >
                  <span className="flex items-center gap-3 font-semibold">
                    <Icon className="w-4 h-4 text-slate-400 group-hover:text-amber-600 transition-colors" />
                    {item.label}
                  </span>
                  <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" />
                </button>
              );
            })}
          </div>
        </div>

        {/* Footer (Figma: Powered by Ordera.no) */}
        <div className="pt-6 border-t border-slate-100 text-center space-y-2">
          <div className="w-8 h-8 rounded-xl bg-amber-400 flex items-center justify-center text-slate-950 font-black text-sm mx-auto shadow-xs">
            <Smile className="w-5 h-5 stroke-[2.5]" />
          </div>
          <div>
            <p className="text-[10px] text-slate-400">Copyright © Agency name</p>
            <p className="text-[11px] font-bold text-slate-700">Powered by Ordera.no</p>
            <p className="text-[9px] text-slate-400 mt-0.5">
              Super App for Restaurant / Takeaway / Cafés
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

