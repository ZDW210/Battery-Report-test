import { BASE_URL, REQUEST_TIMEOUT } from '../utils/config';

let _getToken: (() => string) | null = null;
let _getCustomerId: (() => string) | null = null;
let _redirecting = false;

export function setTokenProvider(fn: () => string) {
  _getToken = fn;
}

export function setCustomerProvider(fn: () => string) {
  _getCustomerId = fn;
}

export function resetRedirectGuard() {
  _redirecting = false;
}

function getToken(): string {
  return _getToken?.() || '';
}

function getCustomerId(): string {
  return _getCustomerId?.() || '';
}

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  data?: any;
  skipAuth?: boolean;
  skipCustomerScope?: boolean;
}

function normalizeResponse<T>(body: any): T {
  if (body && typeof body === 'object' && 'code' in body) {
    if (body.code === 0) return body.data as T;
    throw new Error(body.msg || body.message || 'request failed');
  }
  return body as T;
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, skipAuth, skipCustomerScope } = options;

  const headers: Record<string, string> = { 'tenant-id': '1' };
  if (data != null) headers['Content-Type'] = 'application/json';

  const customerId = skipCustomerScope ? '' : getCustomerId();
  if (customerId) headers['x-energy-customer-id'] = customerId;

  if (!skipAuth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data: data != null && headers['Content-Type'] === 'application/json' ? JSON.stringify(data) : data,
      timeout: REQUEST_TIMEOUT,
      header: headers,
      success(res) {
        const body = res.data as any;
        if (res.statusCode === 401) {
          if (!skipAuth && !_redirecting) {
            _redirecting = true;
            uni.reLaunch({ url: '/pages/auth/login' });
          }
          reject(new Error(skipAuth ? (body?.msg || body?.message || body?.error || 'auth failed') : 'login expired'));
          return;
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            resolve(normalizeResponse<T>(body));
          } catch (error: any) {
            uni.showToast({ title: error.message || '请求失败', icon: 'none' });
            reject(error);
          }
          return;
        }

        const message = body?.msg || body?.message || body?.error || 'request failed';
        uni.showToast({ title: message, icon: 'none' });
        reject(new Error(message));
      },
      fail(err) {
        console.error('Request failed:', err);
        uni.showToast({ title: '网络异常，请检查后端服务', icon: 'none' });
        reject(err);
      },
    });
  });
}

export function get<T = any>(url: string, params?: Record<string, any>, options?: { skipCustomerScope?: boolean }): Promise<T> {
  let query = '';
  const customerId = options?.skipCustomerScope ? '' : getCustomerId();
  const mergedParams = customerId ? { ...params, customerId } : params;
  if (mergedParams) {
    const qs = Object.entries(mergedParams)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
      .join('&');
    if (qs) query = `?${qs}`;
  }
  return request({ url: `${url}${query}`, method: 'GET', skipCustomerScope: options?.skipCustomerScope });
}

export function post<T = any>(url: string, data?: any): Promise<T> {
  return request({ url, method: 'POST', data });
}
