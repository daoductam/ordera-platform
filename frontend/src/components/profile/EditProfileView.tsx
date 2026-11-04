import React, { useState } from "react";
import { ChevronLeft, User, Phone, Mail, Calendar } from "lucide-react";
import type { MemberProfile, UpdateProfileDto } from "@/types/profile";

export interface EditProfileViewProps {
  profile: MemberProfile;
  onBack: () => void;
  onSave: (updated: UpdateProfileDto) => void;
}

export const EditProfileView: React.FC<EditProfileViewProps> = ({
  profile,
  onBack,
  onSave,
}) => {
  const [name, setName] = useState(profile.name);
  const [phone, setPhone] = useState(profile.phone);
  const [email, setEmail] = useState(profile.email);
  const [dob, setDob] = useState(profile.dateOfBirth || "23/08/2000");

  // Kiểm tra xem người dùng đã thay đổi thông tin chưa (State A vs State B)
  const isModified =
    name !== profile.name ||
    phone !== profile.phone ||
    email !== profile.email ||
    dob !== (profile.dateOfBirth || "23/08/2000");

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    onSave({
      name,
      phone,
      email,
      dateOfBirth: dob,
    });
  };

  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-24">
      {/* 1. TOP HEADER (Figma 04.08 & 04.09: <   Edit Profile) */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3.5 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        <button
          onClick={onBack}
          className="p-1 -ml-1 text-slate-800 hover:text-slate-600 active:scale-95 transition-all"
        >
          <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
        </button>

        <h1 className="font-bold text-base text-slate-900 tracking-tight">
          Edit Profile
        </h1>

        {/* Cân bằng layout header */}
        <div className="w-5" />
      </header>

      {/* 2. FORM CÁC TRƯỜNG CHỈNH SỬA CHUẨN FIGMA 04.08 & 04.09 */}
      <form onSubmit={handleSubmit} className="p-4 space-y-3.5 pt-6">
        {/* Trường 1: Họ tên */}
        <div className="bg-[#F4F5F7] rounded-xl px-4 py-3.5 flex items-center gap-3">
          <User className="w-4 h-4 text-slate-400 stroke-[1.8] shrink-0" />
          <input
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="David Nguyen"
            className="bg-transparent border-none outline-none text-xs font-semibold text-slate-800 w-full placeholder:text-slate-400"
          />
        </div>

        {/* Trường 2: Số điện thoại */}
        <div className="bg-[#F4F5F7] rounded-xl px-4 py-3.5 flex items-center gap-3">
          <Phone className="w-4 h-4 text-slate-400 stroke-[1.8] shrink-0" />
          <input
            type="tel"
            required
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="+4797362748"
            className="bg-transparent border-none outline-none text-xs font-semibold text-slate-800 w-full placeholder:text-slate-400"
          />
        </div>

        {/* Trường 3: Email */}
        <div className="bg-[#F4F5F7] rounded-xl px-4 py-3.5 flex items-center gap-3">
          <Mail className="w-4 h-4 text-slate-400 stroke-[1.8] shrink-0" />
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="info@ordera.no"
            className="bg-transparent border-none outline-none text-xs font-semibold text-slate-800 w-full placeholder:text-slate-400"
          />
        </div>

        {/* Trường 4: Ngày sinh */}
        <div className="bg-[#F4F5F7] rounded-xl px-4 py-3.5 flex items-center gap-3">
          <Calendar className="w-4 h-4 text-slate-400 stroke-[1.8] shrink-0" />
          <input
            type="text"
            value={dob}
            onChange={(e) => setDob(e.target.value)}
            placeholder="23/08/2000"
            className="bg-transparent border-none outline-none text-xs font-semibold text-slate-800 w-full placeholder:text-slate-400"
          />
        </div>

        {/* NÚT DONE (Figma 04.08 xám khi chưa sửa, 04.09 vàng cam khi đã sửa) */}
        <div className="pt-2">
          <button
            type="submit"
            className={`w-full py-3.5 rounded-xl font-bold text-sm text-center shadow-2xs transition-all active:scale-[0.99] ${
              isModified
                ? "bg-[#F5B843] hover:bg-[#DEA100] text-slate-900 cursor-pointer shadow-xs"
                : "bg-[#D1D5DB] text-white hover:bg-slate-400 cursor-pointer"
            }`}
          >
            Done
          </button>
        </div>
      </form>
    </div>
  );
};

