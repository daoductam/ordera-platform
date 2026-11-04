import React, { useState, useEffect } from "react";
import { CheckCircle2, Loader2, Smile } from "lucide-react";
import type { OtpChannel } from "@/types/auth";

export interface OtpVerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  channel: OtpChannel;
  onChangeChannel?: (ch: OtpChannel) => void;
  recipientText: string;
  showChannelSelectFirst?: boolean;
  onVerificationSuccess: () => void;
}

export const OtpVerificationModal: React.FC<OtpVerificationModalProps> = ({
  isOpen,
  onClose,
  channel: initialChannel,
  onChangeChannel,
  recipientText,
  showChannelSelectFirst = false,
  onVerificationSuccess,
}) => {
  const [stage, setStage] = useState<"CHANNEL" | "INPUT" | "CHECKING" | "SUCCESS">(
    showChannelSelectFirst ? "CHANNEL" : "INPUT"
  );
  const [selectedChannel, setSelectedChannel] = useState<OtpChannel>(initialChannel);
  const [otpValue, setOtpValue] = useState("");

  useEffect(() => {
    if (isOpen) {
      setStage(showChannelSelectFirst ? "CHANNEL" : "INPUT");
      setOtpValue("");
    }
  }, [isOpen, showChannelSelectFirst]);

  if (!isOpen) return null;

  const handleSelectChannelDone = () => {
    if (onChangeChannel) onChangeChannel(selectedChannel);
    setStage("INPUT");
  };

  const handleVerify = () => {
    if (otpValue.length >= 6) {
      setStage("CHECKING");
      setTimeout(() => {
        setStage("SUCCESS");
      }, 1500);
    }
  };

  const handleDoneSuccess = () => {
    onVerificationSuccess();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white w-full max-w-sm rounded-3xl p-6 shadow-2xl relative animate-in zoom-in-95 text-center space-y-4">
        {/* Ordera Yellow Smile Icon on Top */}
        <div className="w-10 h-10 rounded-xl bg-amber-400 flex items-center justify-center text-slate-950 mx-auto shadow-sm">
          <Smile className="w-6 h-6 stroke-[2.5]" />
        </div>

        {/* 1. STAGE: CHANNEL SELECT (Figma 03.03 & 03.04 Verify Account) */}
        {stage === "CHANNEL" && (
          <div className="space-y-4 animate-in fade-in">
            <div>
              <h3 className="font-extrabold text-base text-slate-900">Verify Account</h3>
              <p className="text-xs text-slate-500 mt-1">
                Choose how to receive your OTP: Email or SMS
              </p>
            </div>

            <div className="grid grid-cols-2 gap-2.5 pt-1">
              <button
                type="button"
                onClick={() => setSelectedChannel("EMAIL")}
                className={`py-3 px-2 rounded-2xl border text-xs font-bold transition-all ${
                  selectedChannel === "EMAIL"
                    ? "bg-amber-400 border-amber-400 text-slate-950 font-black shadow-xs scale-102"
                    : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                }`}
              >
                Email OTP
              </button>
              <button
                type="button"
                onClick={() => setSelectedChannel("SMS")}
                className={`py-3 px-2 rounded-2xl border text-xs font-bold transition-all ${
                  selectedChannel === "SMS"
                    ? "bg-amber-400 border-amber-400 text-slate-950 font-black shadow-xs scale-102"
                    : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                }`}
              >
                SMS OTP
              </button>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-2xl transition-all"
              >
                Back
              </button>
              <button
                type="button"
                onClick={handleSelectChannelDone}
                className="py-3 bg-amber-400 hover:bg-amber-500 text-slate-950 text-xs font-black rounded-2xl shadow-sm transition-all"
              >
                Get code
              </button>
            </div>
          </div>
        )}

        {/* 2. STAGE: OTP INPUT (Figma 02.04 & 02.05 / 03.05 & 03.06) */}
        {stage === "INPUT" && (
          <div className="space-y-4 animate-in fade-in">
            <div>
              <h3 className="font-extrabold text-base text-slate-900">Verify OTP code</h3>
              <p className="text-xs text-slate-500 mt-1">
                OTP number has been sent to{" "}
                <span className="font-bold text-slate-700">{recipientText}</span>
              </p>
            </div>

            <div className="py-2">
              <input
                type="text"
                maxLength={6}
                autoFocus
                value={otpValue}
                onChange={(e) => setOtpValue(e.target.value.replace(/[^0-9]/g, ""))}
                placeholder="------"
                className="w-full text-center tracking-[1em] text-2xl font-mono font-black py-3 px-4 bg-slate-50 border border-slate-200 rounded-2xl focus:outline-hidden focus:border-amber-500 text-slate-900 shadow-inner"
              />
              <p className="text-[11px] text-slate-400 mt-2">
                Tip: Nhập thử mã <strong className="text-amber-600 font-mono">387506</strong> (theo Figma)
              </p>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-1">
              <button
                type="button"
                onClick={showChannelSelectFirst ? () => setStage("CHANNEL") : onClose}
                className="py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-2xl transition-all"
              >
                Back
              </button>
              <button
                type="button"
                disabled={otpValue.length < 6}
                onClick={handleVerify}
                className={`py-3 rounded-2xl text-xs font-black transition-all ${
                  otpValue.length >= 6
                    ? "bg-amber-400 hover:bg-amber-500 text-slate-950 shadow-md shadow-amber-400/20 active:scale-98"
                    : "bg-slate-200 text-slate-400 cursor-not-allowed"
                }`}
              >
                Verify
              </button>
            </div>
          </div>
        )}

        {/* 3. STAGE: CHECKING (Figma 02.06 & 03.08 Đang kiểm tra) */}
        {stage === "CHECKING" && (
          <div className="py-8 space-y-4 animate-in zoom-in-95">
            <h3 className="font-extrabold text-base text-slate-900">Verify OTP code</h3>
            <div className="py-4">
              <Loader2 className="w-12 h-12 text-amber-500 animate-spin mx-auto stroke-[2.5]" />
            </div>
            <p className="text-sm font-bold text-slate-700 tracking-wide">Checking</p>
          </div>
        )}

        {/* 4. STAGE: SUCCESS (Figma 02.07 & 03.09 Thành công) */}
        {stage === "SUCCESS" && (
          <div className="py-4 space-y-4 animate-in zoom-in-95">
            <h3 className="font-extrabold text-base text-slate-900">Verify OTP code</h3>

            <div className="w-14 h-14 bg-amber-50 text-amber-500 rounded-full flex items-center justify-center mx-auto shadow-inner">
              <CheckCircle2 className="w-10 h-10 fill-amber-500 text-white" />
            </div>

            <div className="space-y-1">
              <h4 className="font-black text-lg text-slate-900">Successfull</h4>
              <p className="text-xs text-slate-500 leading-relaxed px-4">
                Welcome to Ordera's Merchants membership. You can sign in now to redeem voucher
                and gifts.
              </p>
            </div>

            <div className="pt-2">
              <button
                type="button"
                onClick={handleDoneSuccess}
                className="w-full py-3.5 bg-amber-400 hover:bg-amber-500 active:scale-98 text-slate-950 font-black text-xs rounded-2xl shadow-md transition-all"
              >
                Continue
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

