import React, { useState } from "react";
import { X, Mail, Phone, CheckCircle2, Send, HelpCircle } from "lucide-react";

export interface NotificationDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NotificationDetailModal: React.FC<NotificationDetailModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [email, setEmail] = useState("henrytran.bi@gmail.com");
  const [phone, setPhone] = useState("+47 97362748");
  const [agreed, setAgreed] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSubmitted(true);
    }, 800);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[92vh] flex flex-col animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-amber-500" />
            <h3 className="font-extrabold text-base text-slate-900">Missing Your Points?</h3>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <div className="p-5 space-y-4 overflow-y-auto flex-1 text-slate-800">
          {/* Explanation Banner */}
          <div className="bg-[#FFFBEB] border border-[#FDE68A] p-4 rounded-2xl space-y-2">
            <p className="text-xs text-amber-950 font-medium leading-relaxed">
              We've upgraded our system to improve your experience. If you don't see your loyalty
              points, please contact us or submit a request — we'll help you retrieve them!
            </p>
            <div className="text-[11px] text-amber-900 font-bold">
              Contact us: Email: <span className="underline">info@example.com</span>
            </div>
          </div>

          {isSubmitted ? (
            <div className="text-center py-6 space-y-3 bg-emerald-50 rounded-2xl border border-emerald-100 p-4">
              <div className="w-12 h-12 rounded-full bg-emerald-500 text-white flex items-center justify-center mx-auto shadow-md">
                <CheckCircle2 className="w-7 h-7" />
              </div>
              <h4 className="font-black text-sm text-emerald-950">Yêu cầu đã được gửi!</h4>
              <p className="text-xs text-emerald-800 leading-relaxed">
                Đội ngũ hỗ trợ đã nhận thông tin và sẽ kiểm tra các giao dịch trước đây để đồng bộ
                điểm vào tài khoản của bạn trong vòng 24 giờ.
              </p>
              <button
                onClick={onClose}
                className="mt-2 px-5 py-2 rounded-xl bg-emerald-600 text-white font-bold text-xs"
              >
                Đồng ý
              </button>
            </div>
          ) : (
            /* Form gửi yêu cầu khôi phục điểm */
            <form onSubmit={handleSubmit} className="space-y-3.5">
              <div className="space-y-1">
                <label className="text-[11px] font-bold text-slate-700 flex items-center gap-1.5">
                  <Mail className="w-3.5 h-3.5 text-slate-400" />
                  <span>Email của tài khoản cũ</span>
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 font-medium focus:outline-hidden focus:border-amber-500"
                />
              </div>

              <div className="space-y-1">
                <label className="text-[11px] font-bold text-slate-700 flex items-center gap-1.5">
                  <Phone className="w-3.5 h-3.5 text-slate-400" />
                  <span>Số điện thoại cũ (nếu có)</span>
                </label>
                <input
                  type="text"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-xs text-slate-900 font-medium focus:outline-hidden focus:border-amber-500"
                />
              </div>

              <label className="flex items-start gap-2 pt-1 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={agreed}
                  onChange={(e) => setAgreed(e.target.checked)}
                  className="mt-0.5 w-4 h-4 rounded text-amber-500 focus:ring-amber-400"
                />
                <span className="text-[11px] text-slate-500 leading-tight">
                  Agree to receive Ordera's updates, promotion & Terms of Services
                </span>
              </label>

              <div className="pt-2 flex gap-2">
                <a
                  href="mailto:info@example.com"
                  className="flex-1 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 font-bold text-xs text-slate-700 flex items-center justify-center gap-1 transition-all"
                >
                  <Mail className="w-3.5 h-3.5" />
                  <span>Emails us</span>
                </a>

                <button
                  type="submit"
                  disabled={!agreed || isSubmitting}
                  className="flex-1 py-2.5 rounded-xl bg-amber-400 hover:bg-amber-500 disabled:opacity-50 text-slate-950 font-black text-xs flex items-center justify-center gap-1 transition-all active:scale-98 shadow-xs"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>{isSubmitting ? "Sending..." : "Submit Form"}</span>
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

