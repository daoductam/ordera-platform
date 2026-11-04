import React, { useState } from "react";
import { X, Check } from "lucide-react";
import type { MemberProfile, UpdateProfileDto } from "@/types/profile";

export interface EditProfileDrawerProps {
  isOpen: boolean;
  profile: MemberProfile;
  onClose: () => void;
  onSave: (updated: UpdateProfileDto) => void;
}

export const EditProfileDrawer: React.FC<EditProfileDrawerProps> = ({
  isOpen,
  profile,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState(profile.name);
  const [email, setEmail] = useState(profile.email);
  const [phone, setPhone] = useState(profile.phone.replace("+47", "").trim());
  const [dob, setDob] = useState(profile.dateOfBirth || "23/08/2000");
  const [password, setPassword] = useState("");
  const [savedSuccess, setSavedSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => {
      onSave({
        name,
        email,
        phone: `+47 ${phone}`,
        dateOfBirth: dob,
        password: password || undefined,
      });
      setSavedSuccess(false);
      onClose();
    }, 600);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white rounded-t-3xl sm:rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col animate-in slide-in-from-bottom-6">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-extrabold text-base text-slate-900">Edit Profile</h3>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4 overflow-y-auto">
          {/* Name */}
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

          {/* Email */}
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

          {/* Phone */}
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

          {/* DOB */}
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

          {/* Password */}
          <div className="space-y-1">
            <label className="text-[11px] font-bold text-slate-700">Change Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Leave empty to keep current password"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 focus:outline-hidden focus:border-amber-500 font-medium"
            />
            <p className="text-[10px] text-slate-400">
              At least 6 characters, included number & alphabet
            </p>
          </div>

          {/* Action buttons */}
          <div className="pt-2">
            <button
              type="submit"
              className={`w-full py-3 rounded-2xl font-black text-xs transition-all flex items-center justify-center gap-1.5 shadow-sm active:scale-98 ${
                savedSuccess
                  ? "bg-emerald-600 text-white"
                  : "bg-amber-400 hover:bg-amber-500 text-slate-950"
              }`}
            >
              {savedSuccess ? (
                <>
                  <Check className="w-4 h-4 stroke-[3]" />
                  <span>Saved Successfully!</span>
                </>
              ) : (
                <span>Done</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

