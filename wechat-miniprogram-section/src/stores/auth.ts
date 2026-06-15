import { defineStore } from 'pinia';
import { ref } from 'vue';
import { setTokenProvider, resetRedirectGuard } from '../api/request';
import { login as loginApi, wechatLogin as wechatLoginApi, getProfile } from '../api/auth';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

export interface UserInfo {
  id: string;
  username: string;
  displayName: string;
  role: string;
  miniAdminEnabled: boolean;
}

function readStoredUser(): UserInfo | null {
  const value = uni.getStorageSync(USER_KEY);
  if (!value) return null;
  if (typeof value === 'string') {
    try {
      return JSON.parse(value) as UserInfo;
    } catch (e) {
      return null;
    }
  }
  return value as UserInfo;
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(uni.getStorageSync(TOKEN_KEY) || '');
  const user = ref<UserInfo | null>(readStoredUser());
  const isLoggedIn = ref(!!token.value);

  setTokenProvider(() => token.value);

  async function login(username: string, password: string): Promise<void> {
    const result = await loginApi(username, password);
    applyLoginResult(result);
  }

  async function wechatLogin(payload: { code: string; nickname?: string; avatar?: string }): Promise<void> {
    const result = await wechatLoginApi(payload);
    applyLoginResult(result);
  }

  async function refreshProfile(): Promise<void> {
    if (!token.value) return;
    const profile = await getProfile();
    user.value = profile;
    uni.setStorageSync(USER_KEY, JSON.stringify(profile));
  }

  function applyLoginResult(result: Awaited<ReturnType<typeof loginApi>>) {
    token.value = result.token;
    user.value = result.user;
    isLoggedIn.value = true;
    uni.setStorageSync(TOKEN_KEY, result.token);
    uni.setStorageSync(USER_KEY, JSON.stringify(result.user));
    resetRedirectGuard();
  }

  function logout() {
    token.value = '';
    user.value = null;
    isLoggedIn.value = false;
    uni.removeStorageSync(TOKEN_KEY);
    uni.removeStorageSync(USER_KEY);
    uni.reLaunch({ url: '/pages/auth/login' });
  }

  return { token, user, isLoggedIn, login, wechatLogin, refreshProfile, logout };
});
