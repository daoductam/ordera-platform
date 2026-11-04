import React, { useState } from "react";
import { Award, QrCode, Clock, ShieldCheck, ChevronRight, ArrowUpRight, ArrowDownLeft } from "lucide-react";
import { MOCK_CURRENT_USER, MOCK_TIERS, MOCK_LEDGER_ENTRIES } from "@/mock/mockData";
import { formatPoints } from "@/lib/utils";
import type { TierLevelDto } from "@/types";

export const TierPage: React.FC = () => {
  const [selectedTier, setSelectedTier] = useState<TierLevelDto>(MOCK_CURRENT_USER.tier);
  const [showClaimModal, setShowClaimModal] = useState(false);
  const [claimReceiptCode, setClaimReceiptCode] = useState("");
  const [claimSuccess, setClaimSuccess] = useState(false);

  const user = MOCK_CURRENT_USER;

  const handleClaim = (e: React.FormEvent) => {
    e.preventDefault();
    if (!claimReceiptCode) return;
    setClaimSuccess(true);
    setTimeout(() => {
      setClaimSuccess(false);
      setShowClaimModal(false);
      setClaimReceiptCode("");
    }, 2000);
  };

  return (
    <div className="p-4 space-y-4">
      {/* THẺ THÀNH VIÊN KỸ THUẬT SỐ (Figma 04.10 - 04.15) */}
      <div
        className={`relative overflow-hidden rounded-3xl p-5 text-white shadow-xl bg-linear-to-br ${user.tier.gradient}`}
      >
        <div className="flex justify-between items-start">
          <div>
            <span className="text-[10px] font-black tracking-widest uppercase opacity-80">
              Ordera Loyalty Program
            </span>
            <h2 className="text-xl font-black mt-0.5">{user.fullName}</h2>
            <p className="text-xs opacity-90 font-mono mt-0.5">{user.phone}</p>
          </div>
          <span className="bg-white/20 backdrop-blur-md px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider border border-white/20">
            {user.tier.name}
          </span>
        </div>

        {/* Số Dư Điểm */}
        <div className="mt-6 flex items-baseline justify-between">
          <div>
            <p className="text-[11px] uppercase font-bold opacity-80">Điểm khả dụng</p>
            <p className="text-3xl font-black tracking-tight">
              {formatPoints(user.currentPoints)}{" "}
              <span className="text-sm font-semibold opacity-90">pts</span>
            </p>
          </div>
          <button className="bg-white/20 hover:bg-white/30 backdrop-blur-md p-2 rounded-xl transition-all">
            <QrCode className="w-6 h-6" />
          </button>
        </div>

        {/* Thanh Tiến Trình Thăng Hạng */}
        {user.nextTier && (
          <div className="mt-4 pt-3 border-t border-white/20">
            <div className="flex justify-between text-[11px] font-bold opacity-90 mb-1">
              <span>Tiến trình lên {user.nextTier.name}</span>
              <span>Còn thiếu {formatPoints(user.nextTier.pointsRemaining)} pts</span>
            </div>
            <div className="w-full bg-black/20 rounded-full h-2 overflow-hidden">
              <div
                className="bg-white h-full rounded-full transition-all duration-500 shadow-xs"
                style={{ width: `${user.nextTier.progressPercentage}%` }}
              />
            </div>
          </div>
        )}
      </div>

      {/* KHÁM PHÁ ĐẶC QUYỀN 6 HẠNG THẺ (New -> Ruby) */}
      <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-slate-900 flex items-center gap-1.5">
            <Award className="w-4 h-4 text-orange-600" />
            Đặc quyền 6 Hạng Thành Viên
          </h3>
          <span className="text-[11px] text-slate-400">Nhấn để xem quyền lợi</span>
        </div>

        {/* 6 Tab Hạng Thẻ */}
        <div className="grid grid-cols-3 sm:grid-cols-6 gap-1.5">
          {MOCK_TIERS.map((tier) => (
            <button
              key={tier.code}
              onClick={() => setSelectedTier(tier)}
              className={`py-2 px-1 rounded-xl text-center text-xs font-bold transition-all ${
                selectedTier.code === tier.code
                  ? "bg-slate-900 text-white shadow-xs scale-102"
                  : "bg-slate-50 text-slate-600 hover:bg-slate-100"
              }`}
            >
              <div
                className="w-2.5 h-2.5 rounded-full mx-auto mb-1"
                style={{ backgroundColor: tier.colorHex }}
              />
              <span className="text-[11px]">{tier.name.replace(" Member", "")}</span>
            </button>
          ))}
        </div>

        {/* Chi tiết đặc quyền của hạng đang chọn */}
        <div className="bg-slate-50 rounded-xl p-3 border border-slate-100">
          <div className="flex justify-between items-center mb-2">
            <span className="font-bold text-xs text-slate-900">
              Quyền lợi hạng {selectedTier.name}
            </span>
            <span className="text-[11px] font-black text-orange-600 bg-orange-50 px-2 py-0.5 rounded-md">
              Tích {selectedTier.earnRate}%
            </span>
          </div>
          <ul className="space-y-1.5">
            {selectedTier.perks.map((perk, i) => (
              <li key={i} className="flex items-start gap-2 text-xs text-slate-600">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 shrink-0 mt-0.5" />
                <span>{perk}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* SỔ CÁI BIẾN ĐỘNG ĐIỂM (Points Ledger - Figma 04.03) */}
      <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-extrabold text-sm text-slate-900 flex items-center gap-1.5">
            <Clock className="w-4 h-4 text-slate-600" />
            Lịch sử tích & tiêu điểm
          </h3>
          <button
            onClick={() => setShowClaimModal(true)}
            className="text-xs font-bold text-orange-600 hover:underline flex items-center gap-0.5"
          >
            Khiếu nại thiếu điểm <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="divide-y divide-slate-100">
          {MOCK_LEDGER_ENTRIES.map((entry) => {
            const isAdd = entry.pointsDelta > 0;
            return (
              <div key={entry.id} className="py-2.5 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                      isAdd ? "bg-emerald-50 text-emerald-600" : "bg-red-50 text-red-600"
                    }`}
                  >
                    {isAdd ? (
                      <ArrowDownLeft className="w-4 h-4" />
                    ) : (
                      <ArrowUpRight className="w-4 h-4" />
                    )}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-slate-900">{entry.note}</p>
                    <p className="text-[10px] text-slate-400">{entry.createdAt}</p>
                  </div>
                </div>
                <div className="text-right">
                  <span
                    className={`text-sm font-black ${
                      isAdd ? "text-emerald-600" : "text-slate-900"
                    }`}
                  >
                    {isAdd ? `+${entry.pointsDelta}` : entry.pointsDelta} pts
                  </span>
                  <p className="text-[10px] text-slate-400">
                    Dư: {formatPoints(entry.balanceAfter)}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* MODAL KHIẾU NẠI THIẾU ĐIỂM (Figma 05 · Hỗ trợ điểm) */}
      {showClaimModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-sm rounded-2xl p-5 shadow-2xl">
            <h3 className="font-bold text-base text-slate-900">Khiếu nại thiếu điểm loyalty</h3>
            <p className="text-xs text-slate-500 mt-1">
              Nhập mã hóa đơn mua hàng tại quầy để hệ thống kiểm tra và tự động cộng bù điểm.
            </p>

            {claimSuccess ? (
              <div className="my-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 shrink-0" />
                <span>Gửi phiếu khiếu nại thành công! Điểm sẽ được cộng bù trong 15 phút.</span>
              </div>
            ) : (
              <form onSubmit={handleClaim} className="space-y-3 my-4">
                <div>
                  <label className="text-xs font-bold text-slate-700 block mb-1">
                    Mã hóa đơn / Order ID
                  </label>
                  <input
                    type="text"
                    required
                    value={claimReceiptCode}
                    onChange={(e) => setClaimReceiptCode(e.target.value)}
                    placeholder="Ví dụ: #ORD-9821"
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs focus:outline-hidden focus:border-orange-500"
                  />
                </div>
                <div className="flex gap-2 justify-end pt-2">
                  <button
                    type="button"
                    onClick={() => setShowClaimModal(false)}
                    className="px-3 py-2 text-xs font-bold text-slate-600 rounded-xl hover:bg-slate-100"
                  >
                    Đóng
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 text-xs font-bold text-white bg-orange-600 rounded-xl hover:bg-orange-700 shadow-xs"
                  >
                    Gửi yêu cầu
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
