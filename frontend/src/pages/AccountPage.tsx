import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { MemberProfile, MemberTierName } from "@/types/profile";
import {
  MOCK_NEW_PROFILE,
  MOCK_SILVER_PROFILE,
  MOCK_TIER_PROFILES,
} from "@/mock/profileMock";
import { AccountOverview } from "@/components/profile/AccountOverview";
import { ProfileDetailView } from "@/components/profile/ProfileDetailView";
import { EditProfileDrawer } from "@/components/profile/EditProfileDrawer";
import { UpdateProfileView } from "@/components/profile/UpdateProfileView";
import { ExtendedAccountDrawer } from "@/components/profile/ExtendedAccountDrawer";
import { TierDetailModal } from "@/components/profile/TierDetailModal";
import { NotificationListView } from "@/components/notifications/NotificationListView";
import { NotificationDetailModal } from "@/components/notifications/NotificationDetailModal";

export const AccountPage: React.FC = () => {
  const navigate = useNavigate();

  // Profile đang được xem (mặc định là David Nguyen - Silver chuẩn Figma 04.02 / 04.03)
  const [profile, setProfile] = useState<MemberProfile>(MOCK_SILVER_PROFILE);

  // Màn hình view chính đang hiển thị
  const [viewMode, setViewMode] = useState<
    "overview" | "profileDetail" | "updateProfile" | "notifications"
  >("overview");

  // Modals & Drawers
  const [isEditDrawerOpen, setIsEditDrawerOpen] = useState(false);
  const [isExtendedMenuOpen, setIsExtendedMenuOpen] = useState(false);
  const [isTierModalOpen, setIsTierModalOpen] = useState(false);
  const [selectedTierForModal, setSelectedTierForModal] = useState<MemberTierName>("Silver");
  const [isMissingPointsModalOpen, setIsMissingPointsModalOpen] = useState(false);

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center">
      <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl">
        {/* VIEW 1: TỔNG QUAN TÀI KHOẢN (Figma 04.01, 04.02, 04.03, 05.01) */}
        {viewMode === "overview" && (
          <AccountOverview
            profile={profile}
            onBack={() => navigate("/")}
            onOpenNotifications={() => setViewMode("notifications")}
            onOpenProfileDetail={() => setViewMode("profileDetail")}
            onOpenVouchers={() => navigate("/rewards")}
            onOpenGifts={() => navigate("/rewards")}
            onOpenOrders={() => navigate("/orders")}
            onOpenTierModal={(tier) => {
              setSelectedTierForModal(tier);
              setIsTierModalOpen(true);
            }}
            onSelectTier={(tier) => {
              setProfile(MOCK_TIER_PROFILES[tier] || MOCK_SILVER_PROFILE);
            }}
            onLearnMoreMissingPoints={() => setIsMissingPointsModalOpen(true)}
            onLogout={() => {
              setProfile(MOCK_NEW_PROFILE);
              navigate("/auth");
            }}
          />
        )}

        {/* VIEW 2: CHI TIẾT HỒ SƠ (Figma 04.07) */}
        {viewMode === "profileDetail" && (
          <ProfileDetailView
            profile={profile}
            onBack={() => setViewMode("overview")}
            onEdit={() => setIsEditDrawerOpen(true)}
          />
        )}

        {/* VIEW 3: BỔ SUNG THÔNG TIN (Figma 04.04) */}
        {viewMode === "updateProfile" && (
          <UpdateProfileView
            onBack={() => setViewMode("overview")}
            onSave={(updated) => {
              setProfile((prev) => ({
                ...prev,
                name: updated.name,
                email: updated.email,
                phone: updated.phone,
                dateOfBirth: updated.dateOfBirth,
              }));
              setViewMode("overview");
            }}
          />
        )}

        {/* VIEW 4: DANH SÁCH THÔNG BÁO (Figma 05.02) */}
        {viewMode === "notifications" && (
          <NotificationListView
            onBack={() => setViewMode("overview")}
            onSelectNotification={() => setIsMissingPointsModalOpen(true)}
            onOpenSupportModal={() => setIsMissingPointsModalOpen(true)}
          />
        )}

        {/* DRAWER CHỈNH SỬA HỒ SƠ (Figma 04.08 & 04.09) */}
        <EditProfileDrawer
          isOpen={isEditDrawerOpen}
          profile={profile}
          onClose={() => setIsEditDrawerOpen(false)}
          onSave={(updated) => {
            setProfile((prev) => ({
              ...prev,
              name: updated.name,
              email: updated.email,
              phone: updated.phone,
              dateOfBirth: updated.dateOfBirth,
            }));
          }}
        />

        {/* DRAWER MENU MỞ RỘNG (Figma 04.06) */}
        <ExtendedAccountDrawer
          isOpen={isExtendedMenuOpen}
          profile={profile}
          onClose={() => setIsExtendedMenuOpen(false)}
          onOpenProfileDetail={() => setViewMode("profileDetail")}
          onOpenVouchers={() => navigate("/rewards")}
          onOpenGifts={() => navigate("/rewards")}
          onOpenOrders={() => navigate("/orders")}
          onLogout={() => {
            setProfile(MOCK_NEW_PROFILE);
            navigate("/auth");
          }}
        />

        {/* MODAL CHI TIẾT 6 HẠNG THÀNH VIÊN (Figma 04.10 - 04.15) */}
        <TierDetailModal
          isOpen={isTierModalOpen}
          tier={selectedTierForModal}
          currentPoints={profile.currentPoints}
          onClose={() => setIsTierModalOpen(false)}
          onSelectTier={(newTier) => {
            setSelectedTierForModal(newTier);
            setProfile(MOCK_TIER_PROFILES[newTier] || MOCK_SILVER_PROFILE);
          }}
        />

        {/* MODAL HỖ TRỢ THIẾU ĐIỂM (Figma 05.03) */}
        <NotificationDetailModal
          isOpen={isMissingPointsModalOpen}
          onClose={() => setIsMissingPointsModalOpen(false)}
        />
      </div>
    </div>
  );
};

