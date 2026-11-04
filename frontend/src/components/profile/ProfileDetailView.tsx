import React from "react";
import { ChevronLeft, Camera } from "lucide-react";
import type { MemberProfile } from "@/types/profile";

export interface ProfileDetailViewProps {
  profile: MemberProfile;
  onBack: () => void;
  onEdit: () => void;
}

export const ProfileDetailView: React.FC<ProfileDetailViewProps> = ({
  profile,
  onBack,
  onEdit,
}) => {
  return (
    <div className="bg-white min-h-screen text-slate-900 font-sans pb-24">
      {/* 1. TOP HEADER (Figma 04.07: <   Profile   Edit) */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md px-4 py-3.5 flex items-center justify-between border-b border-slate-100 shadow-2xs">
        <button
          onClick={onBack}
          className="p-1 -ml-1 text-slate-800 hover:text-slate-600 active:scale-95 transition-all"
        >
          <ChevronLeft className="w-5 h-5 stroke-[2.5]" />
        </button>

        <h1 className="font-bold text-base text-slate-900 tracking-tight">
          Profile
        </h1>

        <button
          onClick={onEdit}
          className="text-sm font-semibold text-[#DEA100] hover:text-amber-600 active:scale-95 transition-all px-1 py-0.5"
        >
          Edit
        </button>
      </header>

      {/* 2. AVATAR & NAME */}
      <div className="pt-8 pb-6 flex flex-col items-center">
        <div className="relative">
          <img
            src={
              profile.avatarUrl ||
              "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=240&auto=format&fit=crop&q=80"
            }
            alt={profile.name}
            className="w-28 h-28 rounded-full object-cover shadow-xs border border-slate-100"
          />
          {/* Badge camera màu vàng ở góc dưới phải avatar */}
          <button
            onClick={onEdit}
            title="Change photo"
            className="absolute bottom-0 right-0.5 w-7 h-7 rounded-full bg-[#DEA100] border-2 border-white flex items-center justify-center shadow-xs active:scale-90 transition-all cursor-pointer"
          >
            <Camera className="w-3.5 h-3.5 text-white" />
          </button>
        </div>

        <h2 className="mt-4 font-bold text-lg text-slate-900 tracking-tight text-center">
          {profile.name}
        </h2>
      </div>

      {/* 3. PROFILE INFO CONTAINER (Figma 04.07: Card bo góc chứa Phone, Email, Date of Birth) */}
      <div className="px-4">
        <div className="bg-[#F8F9FA] rounded-2xl p-4 divide-y divide-slate-200/60 shadow-2xs">
          {/* Row 1: Phone + Blue Verified Badge */}
          <div className="py-3.5 flex items-center text-xs">
            <span className="w-28 text-slate-500 font-medium shrink-0">
              Phone
            </span>
            <span className="flex-1 font-semibold text-slate-900">
              {profile.phone}
            </span>
            {/* Scalloped blue verified checkmark icon */}
            <svg
              className="w-4 h-4 text-[#0095F6] shrink-0 fill-current"
              viewBox="0 0 24 24"
            >
              <path d="M10.007 2.104a2.25 2.25 0 013.986 0l.487.89c.328.601.956.98 1.636.985l1.018.006a2.25 2.25 0 012.343 2.343l.006 1.018c.006.68.384 1.308.985 1.636l.89.487a2.25 2.25 0 010 3.986l-.89.487c-.601.328-.98.956-.985 1.636l-.006 1.018a2.25 2.25 0 01-2.343 2.343l-1.018.006c-.68.006-1.308.384-1.636.985l-.487.89a2.25 2.25 0 01-3.986 0l-.487-.89c-.328-.601-.956-.98-1.636-.985l-1.018-.006a2.25 2.25 0 01-2.343-2.343l-.006-1.018c-.006-.68-.384-1.308-.985-1.636l-.89-.487a2.25 2.25 0 010-3.986l.89-.487c.601-.328.98-.956.985-1.636l.006-1.018a2.25 2.25 0 012.343-2.343l1.018-.006c.68-.006 1.308-.384 1.636-.985l.487-.89z" />
              <path
                d="M8.75 12l2.25 2.25 4.5-4.5"
                stroke="white"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                fill="none"
              />
            </svg>
          </div>

          {/* Row 2: Email */}
          <div className="py-3.5 flex items-center text-xs">
            <span className="w-28 text-slate-500 font-medium shrink-0">
              Email
            </span>
            <span className="flex-1 font-semibold text-slate-900 truncate">
              {profile.email}
            </span>
          </div>

          {/* Row 3: Date of Birth */}
          <div className="py-3.5 flex items-center text-xs">
            <span className="w-28 text-slate-500 font-medium shrink-0">
              Date of Birth:
            </span>
            <span className="flex-1 font-semibold text-slate-900">
              {profile.dateOfBirth || "23/08/2000"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
