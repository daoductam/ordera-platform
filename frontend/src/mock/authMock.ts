import type { AuthUser, RegisterFormData } from "@/types/auth";

export const MOCK_GUEST_USER: AuthUser = {
  id: "guest",
  name: "Khách vãng lai",
  phone: "",
  email: "",
  tierName: "Guest",
  currentPoints: 0,
  lifetimePoints: 0,
  isLoggedIn: false,
};

export const MOCK_NEW_REGISTERED_USER: AuthUser = {
  id: "usr_new_aby",
  name: "ABY TRAN",
  phone: "+47 91285249",
  email: "abytran@orderaapp.com",
  dob: "23/08/2000",
  tierName: "New",
  currentPoints: 0,
  lifetimePoints: 0,
  isLoggedIn: true,
};

export const MOCK_LOGGED_IN_USER: AuthUser = {
  id: "usr_active_tam",
  name: "Đào Tâm Đức",
  phone: "+47 98812345",
  email: "tamdao@orderaapp.com",
  dob: "17/04/1998",
  tierName: "Gold",
  currentPoints: 2450,
  lifetimePoints: 2450,
  isLoggedIn: true,
};

export const DEFAULT_REGISTER_FORM: RegisterFormData = {
  name: "Aby Tran",
  email: "abytran@orderaapp.com",
  phone: "91285249",
  dob: "23/08/2000",
};

