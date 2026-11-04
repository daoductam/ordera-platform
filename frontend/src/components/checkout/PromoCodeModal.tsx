import React, { useState } from "react";
import { X, Tag, CheckCircle2, AlertCircle } from "lucide-react";

export interface PromoCodeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApply: (code: string) => boolean; // return true if valid
}

export const PromoCodeModal: React.FC<PromoCodeModalProps> = ({
  isOpen,
  onClose,
  onApply,
}) => {
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim()) return;

    const isValid = onApply(code.trim().toUpperCase());
    if (isValid) {
      setSuccess(true);
      setError(null);
      setTimeout(() => {
        setSuccess(false);
        setCode("");
        onClose();
      }, 700);
    } else {
      setError("Mã ưu đãi không hợp lệ hoặc đã hết hạn.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95">
        {/* Header (Figma 08.05: Add Promo Code) */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Tag className="w-4 h-4 text-amber-500" />
            <h3 className="font-extrabold text-sm text-slate-900">Add Promo Code</h3>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          <div className="space-y-1.5">
            <label className="text-[11px] font-bold text-slate-700">Enter Promo Code</label>
            <input
              type="text"
              autoFocus
              value={code}
              onChange={(e) => {
                setCode(e.target.value);
                setError(null);
              }}
              placeholder="e.g. DISCOUNT30% or VOUCHER100"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 uppercase font-black tracking-wider focus:outline-hidden focus:border-amber-500"
            />
            {error && (
              <p className="text-[11px] text-red-500 font-medium flex items-center gap-1 pt-0.5">
                <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                <span>{error}</span>
              </p>
            )}
          </div>

          <div className="flex gap-2 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-slate-200 text-slate-700 font-bold text-xs hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className={`flex-1 py-2.5 rounded-xl font-black text-xs transition-all active:scale-98 shadow-xs flex items-center justify-center gap-1.5 ${
                success
                  ? "bg-emerald-600 text-white"
                  : "bg-amber-400 hover:bg-amber-500 text-slate-950"
              }`}
            >
              {success ? (
                <>
                  <CheckCircle2 className="w-3.5 h-3.5 stroke-[3]" />
                  <span>Applied!</span>
                </>
              ) : (
                <span>Apply</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

