import React, { useState, useEffect } from "react";
import { Delete, ArrowRight, RotateCcw, CheckCircle, UserCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { formatPoints } from "@/lib/utils";

export const KioskPage: React.FC = () => {
  const [phoneNumber, setPhoneNumber] = useState("");
  const [countdown, setCountdown] = useState(60);
  const [step, setStep] = useState<"PHONE" | "PROFILE" | "SUCCESS">("PHONE");
  const [recognizedUser, setRecognizedUser] = useState<{
    name: string;
    points: number;
    tier: string;
  } | null>(null);

  // Đếm ngược 60s tự động Reset phiên Kiosk (Figma: Apply Counting time, Start Over là Logout luôn)
  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          handleStartOver();
          return 60;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const handleKeyPress = (num: string) => {
    if (phoneNumber.length < 10) {
      setPhoneNumber((prev) => prev + num);
      setCountdown(60); // Reset timeout on touch
    }
  };

  const handleDelete = () => {
    setPhoneNumber((prev) => prev.slice(0, -1));
    setCountdown(60);
  };

  const handleStartOver = () => {
    setPhoneNumber("");
    setStep("PHONE");
    setRecognizedUser(null);
    setCountdown(60);
  };

  const handleLookup = () => {
    if (phoneNumber.length >= 9) {
      // Giả lập nhận diện khách hàng qua SĐT (Figma 07.03 Tích điểm)
      setRecognizedUser({
        name: "Đào Tâm Đức",
        points: 2450,
        tier: "Gold Member",
      });
      setStep("PROFILE");
    }
  };

  const handleSkip = () => {
    setRecognizedUser({
      name: "Khách tích điểm mới",
      points: 0,
      tier: "New Member",
    });
    setStep("PROFILE");
  };

  const handleCompletePayment = () => {
    setStep("SUCCESS");
    setTimeout(() => {
      handleStartOver();
    }, 5000);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white flex flex-col justify-between p-6 select-none font-sans">
      {/* Top Bar Kiosk */}
      <header className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-orange-600 flex items-center justify-center font-black text-xl text-white">
            O
          </div>
          <div>
            <h1 className="font-black text-lg text-white tracking-wide">
              ORDERA KIOSK POS
            </h1>
            <p className="text-xs text-slate-400">Trạm tự phục vụ & Tích điểm</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <Link
            to="/"
            className="text-xs text-slate-400 hover:text-white px-3 py-1.5 rounded-lg border border-slate-800"
          >
            ← Về Web Mobile
          </Link>
          <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-xl text-xs">
            <span className="text-slate-400">Tự hủy phiên sau:</span>
            <span className="font-mono font-black text-orange-400 text-sm">
              {countdown}s
            </span>
          </div>
          <button
            onClick={handleStartOver}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold transition-all text-slate-200"
          >
            <RotateCcw className="w-3.5 h-3.5" /> Start Over
          </button>
        </div>
      </header>

      {/* Main Kiosk Content */}
      <div className="flex-1 flex flex-col justify-center items-center py-6 max-w-xl mx-auto w-full">
        {step === "PHONE" && (
          <div className="w-full space-y-6 text-center">
            <div>
              <span className="bg-orange-500/20 text-orange-400 text-xs font-black uppercase px-3 py-1 rounded-full border border-orange-500/30">
                Bước 1 / 2
              </span>
              <h2 className="text-2xl font-black mt-3">Nhập số điện thoại tích điểm</h2>
              <p className="text-sm text-slate-400 mt-1">
                Tích ngay 1.5% - 3% giá trị đơn hàng vào tài khoản thành viên
              </p>
            </div>

            {/* Ô Hiển Thị Số Điện Thoại */}
            <div className="bg-slate-900 border-2 border-orange-500/50 rounded-2xl py-4 px-6 text-3xl font-mono font-black tracking-widest text-center min-h-[72px] flex items-center justify-center text-orange-400 shadow-inner">
              {phoneNumber || (
                <span className="text-slate-600 text-xl font-normal">Chạm bàn phím số bên dưới</span>
              )}
            </div>

            {/* Bàn Phím Số Kiosk Cảm Ứng Lớn (Figma: Touch Numpad) */}
            <div className="grid grid-cols-3 gap-3">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                <button
                  key={num}
                  onClick={() => handleKeyPress(num.toString())}
                  className="h-16 rounded-2xl bg-slate-900 hover:bg-slate-800 active:scale-95 border border-slate-800 text-2xl font-bold transition-all shadow-xs"
                >
                  {num}
                </button>
              ))}
              <button
                onClick={handleSkip}
                className="h-16 rounded-2xl bg-slate-900/60 hover:bg-slate-800 border border-slate-800/60 text-xs font-bold text-slate-400 uppercase"
              >
                Bỏ qua (Skip)
              </button>
              <button
                onClick={() => handleKeyPress("0")}
                className="h-16 rounded-2xl bg-slate-900 hover:bg-slate-800 active:scale-95 border border-slate-800 text-2xl font-bold transition-all shadow-xs"
              >
                0
              </button>
              <button
                onClick={handleDelete}
                className="h-16 rounded-2xl bg-red-950/40 hover:bg-red-900/40 active:scale-95 border border-red-900/30 flex items-center justify-center text-red-400 transition-all"
              >
                <Delete className="w-6 h-6" />
              </button>
            </div>

            <button
              disabled={phoneNumber.length < 9}
              onClick={handleLookup}
              className={`w-full py-4 rounded-2xl font-black text-base shadow-lg transition-all flex items-center justify-center gap-2 ${
                phoneNumber.length >= 9
                  ? "bg-orange-600 hover:bg-orange-500 text-white shadow-orange-600/30 active:scale-98"
                  : "bg-slate-900 text-slate-600 border border-slate-800 cursor-not-allowed"
              }`}
            >
              Tiếp tục tích điểm <ArrowRight className="w-5 h-5" />
            </button>
          </div>
        )}

        {step === "PROFILE" && recognizedUser && (
          <div className="w-full space-y-6 text-center animate-in fade-in">
            <div className="w-16 h-16 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto border border-emerald-500/30">
              <UserCheck className="w-8 h-8" />
            </div>

            <div>
              <span className="text-xs text-orange-400 font-bold uppercase tracking-wider">
                Đã nhận diện thành viên
              </span>
              <h2 className="text-3xl font-black mt-1">{recognizedUser.name}</h2>
              <p className="text-sm text-slate-400 mt-1">SĐT: {phoneNumber}</p>
            </div>

            <div className="p-4 bg-slate-900 border border-slate-800 rounded-2xl flex items-center justify-around">
              <div>
                <span className="text-xs text-slate-400">Hạng thẻ hiện tại</span>
                <p className="text-base font-bold text-amber-400">{recognizedUser.tier}</p>
              </div>
              <div className="w-px h-8 bg-slate-800" />
              <div>
                <span className="text-xs text-slate-400">Số điểm khả dụng</span>
                <p className="text-base font-bold text-white">
                  {formatPoints(recognizedUser.points)} pts
                </p>
              </div>
            </div>

            {/* Đơn Hàng Tại Quầy */}
            <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-2xl text-left space-y-2 text-xs">
              <div className="flex justify-between text-slate-400">
                <span>01 x Trà Sữa Ô Long Nướng</span>
                <span className="text-white font-bold">55.000đ</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>01 x Bánh Croissant Nướng</span>
                <span className="text-white font-bold">38.000đ</span>
              </div>
              <div className="pt-2 border-t border-slate-800 flex justify-between text-sm">
                <span className="font-bold text-slate-300">Tổng thanh toán:</span>
                <span className="font-black text-orange-400 text-base">93.000đ</span>
              </div>
            </div>

            <button
              onClick={handleCompletePayment}
              className="w-full py-4 bg-orange-600 hover:bg-orange-500 active:scale-98 text-white rounded-2xl font-black text-base shadow-lg shadow-orange-600/30 transition-all"
            >
              Thanh toán & In hóa đơn tích điểm (+140 pts)
            </button>
          </div>
        )}

        {step === "SUCCESS" && (
          <div className="w-full space-y-4 text-center animate-in zoom-in-95">
            <div className="w-20 h-20 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto border border-emerald-500/30">
              <CheckCircle className="w-10 h-10" />
            </div>
            <h2 className="text-3xl font-black">Đặt đơn tại quầy thành công!</h2>
            <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl inline-block">
              <span className="text-xs text-slate-400 block">Số thứ tự nhận món của bạn</span>
              <span className="text-5xl font-black text-orange-500 font-mono tracking-widest mt-1 block">
                #A-08
              </span>
            </div>
            <p className="text-xs text-slate-400">
              Biên nhận đang được in tại máy in nhiệt. Hệ thống sẽ tự động trở về trang chủ sau 5 giây.
            </p>
          </div>
        )}
      </div>

      <footer className="text-center text-xs text-slate-600 border-t border-slate-900 pt-3">
        Powered by OrderaApp.com • Version 2.0 Omni-channel Loyalty Platform
      </footer>
    </div>
  );
};
