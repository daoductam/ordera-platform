import React, { useState } from "react";
import { ChevronLeft, Calendar } from "lucide-react";
import type { RegisterFormData } from "@/types/auth";

export interface RegisterViewProps {
  onBack: () => void;
  onNext: (formData: RegisterFormData) => void;
  onGoToSignIn: () => void;
  initialData?: Partial<RegisterFormData>;
}

export const RegisterView: React.FC<RegisterViewProps> = ({
  onBack,
  onNext,
  onGoToSignIn,
  initialData = {},
}) => {
  const [name, setName] = useState(initialData.name || "");
  const [email, setEmail] = useState(initialData.email || "");
  const [phone, setPhone] = useState(initialData.phone?.replace("+47", "").trim() || "");
  const [dob, setDob] = useState(initialData.dob || "");
  const [countryCode] = useState("+47");

  const isFormValid =
    name.trim().length > 1 &&
    email.includes("@") &&
    phone.trim().length >= 6;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isFormValid) {
      onNext({
        name: name.trim(),
        email: email.trim(),
        phone: `${countryCode} ${phone.trim()}`,
        dob: dob.trim(),
      });
    }
  };

  return (
    <div className="bg-white min-h-screen px-5 pt-3 pb-24 flex flex-col justify-between max-w-md mx-auto font-sans">
      <div>
        {/* 1. Nút Back trên cùng bên trái chuẩn Figma */}
        <button
          onClick={onBack}
          className="flex items-center gap-1 text-xs font-semibold text-slate-700 hover:text-slate-900 transition-colors py-1 cursor-pointer"
        >
          <ChevronLeft className="w-4 h-4 stroke-[2.5]" />
          <span>Back</span>
        </button>

        {/* 2. Tiêu đề Register Account nằm trên Stepper chuẩn Figma 03.01 & 03.02 */}
        <div className="text-center pt-2 pb-4">
          <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
            Register Account
          </h1>
        </div>

        {/* 3. Stepper chuẩn Figma: Nhãn ở TRÊN, vòng tròn mốc và thanh nối ở DƯỚI */}
        <div className="max-w-[270px] mx-auto mb-6">
          {/* Hàng nhãn: Personal info (trái) & Verify account (phải) */}
          <div className="flex justify-between text-[11px] mb-1.5 px-0.5">
            <span className="font-semibold text-slate-800">Personal info</span>
            <span className="font-medium text-slate-400">Verify account</span>
          </div>

          {/* Hàng đường ray và 2 nút tròn mốc */}
          <div className="flex items-center justify-between relative px-4">
            {/* Đường ray nối 2 node */}
            <div className="absolute left-6 right-6 top-1/2 -translate-y-1/2 h-[1px] bg-slate-300 z-0" />

            {/* Mốc 1: Personal info (Vòng tròn viền vàng cam có icon check ✓) */}
            <div className="w-5 h-5 rounded-full border-2 border-[#E5A800] bg-white flex items-center justify-center text-[#E5A800] z-10 shadow-2xs">
              <svg
                className="w-3 h-3 stroke-[2.8]"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4.5 12.75l6 6 9-13.5"
                />
              </svg>
            </div>

            {/* Mốc 2: Verify account (Vòng tròn xám số 2) */}
            <div className="w-5 h-5 rounded-full bg-[#F3F4F6] border border-slate-300 text-slate-400 text-[10px] font-bold flex items-center justify-center z-10">
              2
            </div>
          </div>
        </div>

        {/* 4. Form Fields chuẩn Figma 03.01 (Trống) & 03.02 (Đã điền) */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Your name * */}
          <div className="space-y-1">
            <label className="text-xs font-bold text-slate-700 block">
              Your name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your name"
              className="w-full bg-white border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 transition-all shadow-2xs"
            />
          </div>

          {/* Email * */}
          <div className="space-y-1">
            <label className="text-xs font-bold text-slate-700 block">
              Email <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              className="w-full bg-white border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 transition-all shadow-2xs"
            />
          </div>

          {/* Phone number * */}
          <div className="space-y-1">
            <label className="text-xs font-bold text-slate-700 block">
              Phone number <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-2">
              <div className="flex items-center justify-center px-3 py-2.5 bg-white border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 shrink-0 shadow-2xs">
                {countryCode}
              </div>
              <input
                type="tel"
                required
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/[^0-9]/g, ""))}
                placeholder="Enter your phone number"
                className="flex-1 bg-white border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 transition-all shadow-2xs"
              />
            </div>
          </div>

          {/* Date of birth */}
          <div className="space-y-1">
            <label className="text-xs font-medium text-slate-600 block">
              {dob ? "Date of birth" : "Date of birth (recommend)"}
            </label>
            <div className="relative">
              <input
                type="text"
                value={dob}
                onChange={(e) => setDob(e.target.value)}
                placeholder="DD/MM/YYYY"
                className="w-full bg-white border border-slate-200 rounded-xl pl-3.5 pr-10 py-2.5 text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-hidden focus:border-amber-500 transition-all shadow-2xs"
              />
              <Calendar className="w-4 h-4 text-slate-400 absolute right-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          {/* Nút Next (03.01 xám khi chưa điền, 03.02 vàng cam khi đã điền) */}
          <div className="pt-3">
            <button
              type="submit"
              disabled={!isFormValid}
              className={`w-full py-3.5 rounded-xl font-bold text-sm text-center transition-all shadow-2xs ${
                isFormValid
                  ? "bg-[#F5B843] hover:bg-[#DEA100] active:scale-[0.99] text-slate-900 cursor-pointer shadow-xs"
                  : "bg-[#D1D5DB] text-white cursor-not-allowed"
              }`}
            >
              Next
            </button>
          </div>
        </form>

        {/* Have account? Sign in */}
        <p className="text-center text-xs text-slate-500 pt-4">
          Have account?{" "}
          <button
            onClick={onGoToSignIn}
            className="font-bold text-[#DEA100] hover:text-amber-600 hover:underline cursor-pointer transition-colors"
          >
            Sign in
          </button>
        </p>
      </div>
    </div>
  );
};
