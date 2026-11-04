import React, { useState } from "react";
import type { AuthScreenState, OtpChannel, RegisterFormData, AuthUser } from "@/types/auth";
import {
  MOCK_GUEST_USER,
  MOCK_LOGGED_IN_USER,
  MOCK_NEW_REGISTERED_USER,
  DEFAULT_REGISTER_FORM,
} from "@/mock/authMock";
import { DrawerMenu } from "@/components/auth/DrawerMenu";
import { LoginView } from "@/components/auth/LoginView";
import { RegisterView } from "@/components/auth/RegisterView";
import { OtpVerificationModal } from "@/components/auth/OtpVerificationModal";
import { NewAccountView } from "@/components/auth/NewAccountView";
import { useNavigate } from "react-router-dom";

export const AuthFlowPage: React.FC = () => {
  const [screenState, setScreenState] = useState<AuthScreenState>("DRAWER_GUEST");
  const [currentUser, setCurrentUser] = useState<AuthUser>(MOCK_GUEST_USER);
  const [loginPhone, setLoginPhone] = useState("+47 91285249");
  const [otpChannel, setOtpChannel] = useState<OtpChannel>("SMS");
  const [registerData, setRegisterData] = useState<RegisterFormData>(DEFAULT_REGISTER_FORM);
  const navigate = useNavigate();

  // Navigation handlers
  const handleGoToLogin = () => setScreenState("LOGIN");
  const handleGoToRegister = () => setScreenState("REGISTER_EMPTY");
  const handleGoToProfile = () => setScreenState("NEW_ACCOUNT");

  const handleGetLoginCode = (phone: string) => {
    setLoginPhone(phone);
    setScreenState("OTP_FILLED"); // Modal opens
  };

  const handleRegisterNext = (data: RegisterFormData) => {
    setRegisterData(data);
    setScreenState("CHANNEL_SELECT"); // Channel choice modal
  };

  const handleVerificationSuccess = () => {
    if (screenState === "CHANNEL_SELECT" || screenState === "REGISTER_FILLED" || screenState === "REGISTER_EMPTY") {
      setCurrentUser(MOCK_NEW_REGISTERED_USER);
      setScreenState("NEW_ACCOUNT"); // 03.10
    } else {
      setCurrentUser(MOCK_LOGGED_IN_USER);
      setScreenState("DRAWER_LOGGED_IN"); // 02.02
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-start">
      {/* RENDER MÀN HÌNH TƯƠNG ỨNG FIGMA */}
      <div className="flex-1 flex justify-center bg-slate-900">
        <div className="w-full max-w-md bg-white min-h-screen relative shadow-2xl">
          {/* 1. Màn hình 02.01 & 02.02: Drawer Menu */}
          {(screenState === "DRAWER_GUEST" || screenState === "DRAWER_LOGGED_IN") && (
            <DrawerMenu
              isOpen={true}
              onClose={() => navigate("/")}
              isLoggedIn={currentUser.isLoggedIn}
              onGoToLogin={handleGoToLogin}
              onGoToRegister={handleGoToRegister}
              onGoToProfile={handleGoToProfile}
              onSeeAllShop={() => navigate("/")}
            />
          )}

          {/* 2. Màn hình 02.03: Log In */}
          {screenState === "LOGIN" && (
            <LoginView
              onBack={() => setScreenState("DRAWER_GUEST")}
              onGetCode={handleGetLoginCode}
              onGoToSignUp={handleGoToRegister}
              initialPhone="91285249"
            />
          )}

          {/* 3. Màn hình 03.01 & 03.02: Register Account */}
          {(screenState === "REGISTER_EMPTY" || screenState === "REGISTER_FILLED") && (
            <RegisterView
              onBack={() => setScreenState("LOGIN")}
              onNext={handleRegisterNext}
              onGoToSignIn={handleGoToLogin}
              initialData={screenState === "REGISTER_FILLED" ? DEFAULT_REGISTER_FORM : {}}
            />
          )}

          {/* 4. Màn hình 03.10: Tài khoản mới (New Member Dashboard) */}
          {screenState === "NEW_ACCOUNT" && (
            <NewAccountView
              user={MOCK_NEW_REGISTERED_USER}
              onBack={() => setScreenState("DRAWER_LOGGED_IN")}
              onGoToVouchers={() => navigate("/rewards")}
              onGoToOrders={() => navigate("/cart")}
            />
          )}

          {/* 5. Modals OTP (02.04 - 02.07 & 03.03 - 03.09) */}
          {(screenState === "OTP_EMPTY" ||
            screenState === "OTP_FILLED" ||
            screenState === "CHANNEL_SELECT") && (
            <OtpVerificationModal
              isOpen={true}
              onClose={() => {
                if (screenState === "CHANNEL_SELECT") {
                  setScreenState("REGISTER_FILLED");
                } else {
                  setScreenState("LOGIN");
                }
              }}
              channel={otpChannel}
              onChangeChannel={(ch) => setOtpChannel(ch)}
              recipientText={
                otpChannel === "SMS" ? loginPhone : registerData.email
              }
              showChannelSelectFirst={screenState === "CHANNEL_SELECT"}
              onVerificationSuccess={handleVerificationSuccess}
            />
          )}
        </div>
      </div>
    </div>
  );
};

