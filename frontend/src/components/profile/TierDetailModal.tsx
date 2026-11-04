import React from "react";
import { X, CheckCircle2, Award } from "lucide-react";
import type { MemberTierName } from "@/types/profile";
import { MemberCard } from "./MemberCard";
import { MOCK_TIER_MILESTONES } from "@/mock/profileMock";

export interface TierDetailModalProps {
  isOpen: boolean;
  tier: MemberTierName;
  currentPoints: number;
  onClose: () => void;
  onSelectTier: (tier: MemberTierName) => void;
}

export const TierDetailModal: React.FC<TierDetailModalProps> = ({
  isOpen,
  tier,
  currentPoints,
  onClose,
  onSelectTier,
}) => {
  if (!isOpen) return null;

  const milestone =
    MOCK_TIER_MILESTONES.find((m) => m.tier === tier) || MOCK_TIER_MILESTONES[0];

  const allTiers: MemberTierName[] = ["New", "Silver", "Gold", "Platinum", "Diamond", "Ruby"];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[92vh] flex flex-col animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-500" />
            <h3 className="font-extrabold text-base text-slate-900">
              Hạng thành viên {tier}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* 6 Tabs Hạng thành viên (Figma 04.10 - 04.15) */}
        <div className="px-3 pt-3 flex items-center gap-1.5 overflow-x-auto no-scrollbar border-b border-slate-100 pb-2">
          {allTiers.map((t) => (
            <button
              key={t}
              onClick={() => onSelectTier(t)}
              className={`px-3 py-1.5 rounded-full text-xs font-black whitespace-nowrap transition-all shrink-0 ${
                tier === t
                  ? "bg-slate-900 text-white shadow-xs"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        {/* Body */}
        <div className="p-5 space-y-4 overflow-y-auto flex-1">
          {/* Card preview */}
          <MemberCard tier={tier} currentPoints={currentPoints} />

          {/* Điều kiện đạt hạng */}
          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100 flex items-center justify-between text-xs">
            <span className="text-slate-600 font-semibold">Điểm tích lũy yêu cầu:</span>
            <span className="font-black text-slate-900 text-sm">
              {milestone.requiredPoints} points
            </span>
          </div>

          {/* Danh sách đặc quyền (Perks) */}
          <div className="space-y-2">
            <h4 className="font-black text-xs uppercase tracking-wider text-slate-400">
              Đặc quyền hạng {tier}
            </h4>
            <div className="space-y-2">
              {milestone.perks.map((perk, idx) => (
                <div
                  key={idx}
                  className="p-3 bg-white border border-slate-100 rounded-2xl shadow-2xs flex items-start gap-2.5"
                >
                  <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
                  <span className="text-xs font-bold text-slate-800 leading-snug">{perk}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50">
          <button
            onClick={onClose}
            className="w-full py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs transition-all active:scale-98"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};

