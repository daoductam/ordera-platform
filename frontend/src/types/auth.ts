export type OtpChannel = "SMS" | "EMAIL";

export interface RegisterFormData {
  name: string;
  email: string;
  phone: string;
  dob: string;
}

export interface AuthUser {
  id: string;
  name: string;
  phone: string;
  email: string;
  dob?: string;
  tierName: string;
  currentPoints: number;
  lifetimePoints: number;
  isLoggedIn: boolean;
}

export type AuthScreenState =
  | "DRAWER_GUEST"       // 02.01 Menu - Khách
  | "DRAWER_LOGGED_IN"   // 02.02 Menu - Đã đăng nhập
  | "LOGIN"              // 02.03 Đăng nhập
  | "OTP_EMPTY"          // 02.04 OTP - Chưa nhập
  | "OTP_FILLED"         // 02.05 OTP - Đã nhập
  | "OTP_CHECKING"       // 02.06 Đang xác thực
  | "OTP_SUCCESS"        // 02.07 Xác thực thành công
  | "REGISTER_EMPTY"     // 03.01 Đăng ký - Biểu mẫu trắng
  | "REGISTER_FILLED"    // 03.02 Đăng ký - Đã điền
  | "CHANNEL_SELECT"     // 03.03 & 03.04 Chọn kênh Email/SMS
  | "NEW_ACCOUNT";       // 03.10 Tài khoản mới

