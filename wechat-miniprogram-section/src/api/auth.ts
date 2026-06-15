import { request } from './request';

export interface LoginResult {
  token: string;
  refreshToken?: string;
  expiresTime?: string | number;
  user: {
    id: string;
    username: string;
    displayName: string;
    role: string;
    miniAdminEnabled: boolean;
  };
}

interface WechatLoginPayload {
  code: string;
  nickname?: string;
  avatar?: string;
}

interface RuoYiLoginResult {
  userId: number | string;
  accessToken: string;
  refreshToken?: string;
  expiresTime?: string | number;
  user?: {
    id?: number | string;
    username?: string;
    displayName?: string;
    mobile?: string;
    role?: string;
    miniAdminEnabled?: boolean;
  };
}

function toLoginResult(result: RuoYiLoginResult, fallbackUsername: string): LoginResult {
  return {
    token: result.accessToken,
    refreshToken: result.refreshToken,
    expiresTime: result.expiresTime,
    user: {
      id: String(result.user?.id ?? result.userId),
      username: result.user?.username || fallbackUsername,
      displayName: result.user?.displayName || result.user?.username || fallbackUsername,
      role: result.user?.role || (result.user?.miniAdminEnabled ? 'manager' : 'driver'),
      miniAdminEnabled: !!result.user?.miniAdminEnabled,
    },
  };
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const result = await request<RuoYiLoginResult>({
    url: '/auth/login',
    method: 'POST',
    data: { username, password },
    skipAuth: true,
  });
  return toLoginResult(result, username);
}

export async function wechatLogin(payload: WechatLoginPayload): Promise<LoginResult> {
  const result = await request<RuoYiLoginResult>({
    url: '/auth/wechat-login',
    method: 'POST',
    data: payload,
    skipAuth: true,
  });
  return toLoginResult(result, 'wechat-user');
}

export async function getProfile(): Promise<LoginResult['user']> {
  const result = await request<RuoYiLoginResult['user']>({
    url: '/auth/profile',
    method: 'GET',
  });
  return {
    id: String(result?.id ?? ''),
    username: result?.username || 'wechat-user',
    displayName: result?.displayName || result?.username || '微信用户',
    role: result?.role || (result?.miniAdminEnabled ? 'manager' : 'driver'),
    miniAdminEnabled: !!result?.miniAdminEnabled,
  };
}
