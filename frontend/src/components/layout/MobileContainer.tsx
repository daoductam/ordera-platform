import React from "react";
import { Navbar } from "./Navbar";
import { BottomNav } from "./BottomNav";
import { useLocation } from "react-router-dom";

export const MobileContainer: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const isKiosk = location.pathname === "/kiosk";

  const isDedicatedFigmaPage = [
    "/",
    "/stores",
    "/auth",
    "/account",
    "/profile",
    "/orders",
    "/history",
    "/rewards",
    "/cart",
    "/checkout",
  ].includes(location.pathname);

  return (
    <div className="min-h-screen bg-slate-900 flex justify-center">
      <div
        className={`w-full ${
          isKiosk ? "max-w-4xl min-h-screen bg-slate-950" : "max-w-md min-h-screen bg-slate-50"
        } relative shadow-2xl flex flex-col`}
      >
        {!isKiosk && !isDedicatedFigmaPage && <Navbar />}
        <main className="flex-1">{children}</main>
        {!isKiosk && <BottomNav />}
      </div>
    </div>
  );
};
