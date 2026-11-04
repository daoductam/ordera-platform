import React from "react";
import { HelpCircle } from "lucide-react";

export interface MissingPointsBannerProps {
  onLearnMore: () => void;
  onDismiss?: () => void;
}

export const MissingPointsBanner: React.FC<MissingPointsBannerProps> = ({
  onLearnMore,
}) => {
  return (
    <div className="bg-[#FFFBEB] border border-[#FDE68A] rounded-2xl p-3.5 shadow-2xs space-y-2.5">
      <div className="flex items-start gap-2.5">
        <div className="w-7 h-7 rounded-full bg-amber-100 text-amber-800 flex items-center justify-center shrink-0 mt-0.5">
          <HelpCircle className="w-4 h-4" />
        </div>
        <div className="flex-1 space-y-1">
          <h4 className="font-extrabold text-xs text-amber-950">Missing Your Points?</h4>
          <p className="text-[11px] text-amber-900/80 leading-snug">
            We've upgraded our system to improve your experience. If you don't see your loyalty
            points, please contact us or submit a request — we'll help you retrieve them!
          </p>
        </div>
      </div>

      <div className="flex justify-end">
        <button
          onClick={onLearnMore}
          className="px-3.5 py-1.5 bg-[#FBBF24] hover:bg-[#F59E0B] active:scale-95 text-slate-950 font-black text-xs rounded-full shadow-2xs transition-all"
        >
          Learn more
        </button>
      </div>
    </div>
  );
};

