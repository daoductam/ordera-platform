import React, { useState } from "react";
import { ChevronLeft, Check } from "lucide-react";
import type { UpdateProfileDto } from "@/types/profile";

export interface UpdateProfileViewProps {
  onBack: () => void;
  onSave: (data: UpdateProfileDto) => void;
}

export const UpdateProfileView: React.FC<UpdateProfileViewProps> = ({
  onBack,
  onSave,
}) => {
  const [activeTab, setActiveTab] = useState<"account" | "phone">("account");
  const [name, setName] = useState("David Nguyen");
  const [email, setEmail] = useState("info@ordera.no");
  const [phone, setPhone] = useState("97371618");
  const [dob, setDob] = useState("23/08/2000");
  const [password, setPassword] = useState("••••••••");
  const [rememberMe, setRememberMe] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSuccess(true);
    setTimeout(() => {
      onSave({
        name,
        email,
        phone: `+47 ${phone}`,
        dateOfBirth: dob,
        rememberMe,
      });
    }, 600);
  };

  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-24">
      {/* Header */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 text-slate-900 hover:text-slate-700 active:scale-95 transition-all"
        >
          <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
          <span className="font-black text-sm tracking-tight">Back</span>
        </button>
      </header>

      <div className="p-5 space-y-5">
        {/* Title & subtitle chuẩn Figma 04.04 */}
        <div className="space-y-1">
          <h2 className="text-xl font-black text-slate-900">Update Your Profile</h2>
          <p className="text-xs text-slate-500">
            Updating your information helps us serve you better
          </p>
        </div>

        {/* Stepper (1. Personal info - 2. Verify account) */}
        <div className="flex items-center gap-2 py-1">
          <div className="flex items-center gap-2 text-xs font-bold text-amber-600">
            <span className="w-5 h-5 rounded-full bg-amber-500 text-slate-950 font-black text-[10px] flex items-center justify-center">
              1
            </span>
            <span>Personal Info</span>
          </div>
          <div className="flex-1 h-0.5 bg-slate-200" />
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
            <span className="w-5 h-5 rounded-full bg-slate-200 text-slate-500 text-[10px] flex items-center justify-center font-bold">
              2
            </span>
            <span>Verify account</span>
          </div>
        </div>

        {/* 2 Tabs: By Account / By phone number */}
        <div className="flex bg-slate-100 p-1 rounded-2xl">
          <button
            type="button"
            onClick={() => setActiveTab("account")}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
              activeTab === "account"
                ? "bg-white text-slate-950 shadow-xs font-extrabold"
                : "text-slate-500 hover:text-slate-800"
            }`}
          >
            By Account
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("phone")}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
              activeTab === "phone"
                ? "bg-white text-slate-950 shadow-xs font-extrabold"
                : "text-slate-500 hover:text-slate-800"
            }`}
          >
            By phone number
          </button>
        </div>

        {/* Form Fields */}
        <form onSubmit={handleSubmit} className="space-y-4 pt-1">
          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">Your name *</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your name"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">Email *</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">Phone number *</label>
            <div className="flex gap-2">
              <span className="bg-slate-100 border border-slate-200 rounded-xl px-3 py-2.5 text-xs font-bold text-slate-700 flex items-center shrink-0">
                +47
              </span>
              <input
                type="tel"
                required
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="Enter your phone number"
                className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">
              Date of birth (recommend)
            </label>
            <input
              type="text"
              value={dob}
              onChange={(e) => setDob(e.target.value)}
              placeholder="DD/MM/YYYY"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
            />
          </div>

          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">Password *</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
            />
          </div>

          <div className="flex items-center justify-between pt-1">
            <label className="flex items-center gap-2 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 rounded text-amber-500 focus:ring-amber-400"
              />
              <span className="text-xs text-slate-600 font-medium">Remember me</span>
            </label>
            <button type="button" className="text-xs text-amber-600 font-bold hover:underline">
              Forgot password?
            </button>
          </div>

          <div className="pt-3">
            <button
              type="submit"
              className={`w-full py-3 rounded-2xl font-black text-xs transition-all flex items-center justify-center gap-1.5 shadow-sm active:scale-98 ${
                isSuccess
                  ? "bg-emerald-600 text-white"
                  : "bg-amber-400 hover:bg-amber-500 text-slate-950"
              }`}
            >
              {isSuccess ? (
                <>
                  <Check className="w-4 h-4 stroke-[3]" />
                  <span>Saved!</span>
                </>
              ) : (
                <span>Save</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
