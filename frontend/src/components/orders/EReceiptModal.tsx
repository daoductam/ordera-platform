import React, { useState } from "react";
import { X, Mail, CheckCircle2, Loader2, Send } from "lucide-react";
import type { CustomerOrder } from "@/types/order";

export interface EReceiptModalProps {
  isOpen: boolean;
  order: CustomerOrder | null;
  onClose: () => void;
}

export const EReceiptModal: React.FC<EReceiptModalProps> = ({
  isOpen,
  order,
  onClose,
}) => {
  const [email, setEmail] = useState("henrytran.bi@gmail.com");
  const [step, setStep] = useState<"input" | "sending" | "success">("input");

  if (!isOpen || !order) return null;

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    setStep("sending");
    setTimeout(() => {
      setStep("success");
    }, 1200);
  };

  const handleResetAndClose = () => {
    setStep("input");
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-slate-900">Electronic Receipt</h3>
          <button
            onClick={handleResetAndClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Step 1: Input Email (Figma 06.08) */}
        {step === "input" && (
          <form onSubmit={handleSend} className="p-5 space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto shadow-inner">
              <Mail className="w-6 h-6 stroke-[2]" />
            </div>

            <div className="text-center space-y-1">
              <h4 className="font-extrabold text-sm text-slate-900">
                Send E-Receipt for {order.orderNumber}
              </h4>
              <p className="text-xs text-slate-500 leading-snug">
                The order receipt is being sent to the email address below:
              </p>
            </div>

            <div className="space-y-1 pt-1">
              <label className="text-[11px] font-bold text-slate-700">Email Address *</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 font-medium focus:outline-hidden focus:border-amber-500"
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={handleResetAndClose}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 text-slate-700 font-bold text-xs hover:bg-slate-50"
              >
                Back
              </button>
              <button
                type="submit"
                className="flex-1 py-2.5 rounded-xl bg-amber-400 hover:bg-amber-500 text-slate-950 font-black text-xs flex items-center justify-center gap-1.5 shadow-2xs active:scale-98"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Send</span>
              </button>
            </div>
          </form>
        )}

        {/* Step 2: Sending Loader (Figma 06.09) */}
        {step === "sending" && (
          <div className="p-8 text-center space-y-3">
            <Loader2 className="w-10 h-10 text-amber-500 animate-spin mx-auto" />
            <h4 className="font-extrabold text-sm text-slate-900">Sending E-Receipt...</h4>
            <p className="text-xs text-slate-400">Please wait while we generate and deliver your PDF receipt.</p>
          </div>
        )}

        {/* Step 3: Success Confirmation (Figma 06.10) */}
        {step === "success" && (
          <div className="p-6 text-center space-y-4">
            <div className="w-12 h-12 rounded-full bg-emerald-500 text-white flex items-center justify-center mx-auto shadow-md">
              <CheckCircle2 className="w-7 h-7" />
            </div>
            <div className="space-y-1">
              <h4 className="font-black text-sm text-slate-900">E-Receipt Sent!</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                We've sent a verified receipt for order <span className="font-bold text-slate-800">{order.orderNumber}</span> to <span className="font-bold text-slate-800">{email}</span>.
              </p>
            </div>

            <button
              onClick={handleResetAndClose}
              className="w-full py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs shadow-xs active:scale-98"
            >
              Done
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

