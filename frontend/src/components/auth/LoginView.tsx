import React, { useState } from "react";
import { ChevronLeft } from "lucide-react";

export interface LoginViewProps {
  onBack: () => void;
  onGetCode: (phoneNumber: string) => void;
  onGoToSignUp: () => void;
  initialPhone?: string;
}

export const LoginView: React.FC<LoginViewProps> = ({
  onBack,
  onGetCode,
  onGoToSignUp,
  initialPhone = "",
}) => {
  const [phoneNumber, setPhoneNumber] = useState(initialPhone);
  const [countryCode] = useState("+47");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (phoneNumber.trim().length >= 6) {
      onGetCode(`${countryCode} ${phoneNumber.trim()}`);
    }
  };

  const isButtonEnabled = phoneNumber.trim().length >= 6;

  return (
    <div className="bg-white min-h-screen px-5 pt-3 pb-24 flex flex-col justify-between max-w-md mx-auto">
      <div className="space-y-8">
        {/* Top Back Button */}
        <button
          onClick={onBack}
          className="flex items-center gap-1 text-xs font-bold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
          <span>Back</span>
        </button>

        {/* Title */}
        <div className="text-center pt-2">
          <h2 className="text-2xl font-black text-slate-900 tracking-tight">Log In</h2>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-6 pt-4">
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 block">
              Phone number <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-2">
              <div className="flex items-center justify-center px-3 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-xs font-bold text-slate-800 shrink-0 select-none">
                {countryCode}
              </div>
              <input
                type="tel"
                autoFocus
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value.replace(/[^0-9]/g, ""))}
                placeholder="enter your phone number"
                className="flex-1 bg-slate-50 border border-slate-200 rounded-2xl px-4 py-3 text-sm font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 focus:ring-1 focus:ring-amber-500 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={!isButtonEnabled}
            className={`w-full py-4 rounded-2xl font-black text-xs transition-all shadow-md ${
              isButtonEnabled
                ? "bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-950 shadow-amber-400/20"
                : "bg-slate-200 text-slate-400 cursor-not-allowed"
            }`}
          >
            Get code
          </button>
        </form>

        {/* Footer Switch */}
        <p className="text-center text-xs text-slate-500">
          Don't have account?{" "}
          <button
            onClick={onGoToSignUp}
            className="font-bold text-amber-600 hover:underline"
          >
            Sign up
          </button>
        </p>
      </div>

      <div className="text-center py-4 text-[10px] text-slate-400">
        Ordera Loyalty Program 2.0
      </div>
    </div>
  );
};
