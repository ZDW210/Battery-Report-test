<template>
  <view class="page">
    <view class="brand">
      <view class="logo-mark">E</view>
      <text class="title">移动储能小程序</text>
      <text class="subtitle">微信登录后按后台授权查看客户、项目和设备数据</text>
    </view>

    <view class="card">
      <button class="wechat-btn" :loading="wechatLoading" @tap="handleWechatLogin">
        微信一键登录
      </button>
      <text class="hint">客户归属由后台“用户授权”决定，小程序不会自行放开数据范围。</text>

      <text class="err" v-if="error">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const wechatLoading = ref(false);
const error = ref('');

function wxLogin(): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) {
          resolve(res.code);
          return;
        }
        reject(new Error('微信登录没有返回 code'));
      },
      fail: reject,
    });
  });
}

async function handleWechatLogin() {
  error.value = '';
  wechatLoading.value = true;
  try {
    const code = await wxLogin();
    await auth.wechatLogin({ code });
    uni.switchTab({ url: '/pages/driver/index' });
  } catch (e: any) {
    error.value = e?.message || '微信登录失败，请稍后重试';
  } finally {
    wechatLoading.value = false;
  }
}

</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 80rpx 40rpx 48rpx;
  background: linear-gradient(180deg, #e8fbfb 0%, #f6f8fb 46%, #ffffff 100%);
}
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 70rpx 0 54rpx;
  text-align: center;
}
.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96rpx;
  height: 96rpx;
  color: #ffffff;
  font-size: 48rpx;
  font-weight: 800;
  background: #0bb5b1;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 32rpx rgba(11, 181, 177, 0.24);
}
.title {
  display: block;
  margin-top: 28rpx;
  color: #0f172a;
  font-size: 42rpx;
  font-weight: 800;
}
.subtitle {
  display: block;
  max-width: 560rpx;
  margin-top: 14rpx;
  color: #64748b;
  font-size: 26rpx;
  line-height: 38rpx;
}
.card {
  padding: 36rpx;
  background: #ffffff;
  border: 2rpx solid #e8edf0;
  border-radius: 18rpx;
  box-shadow: 0 16rpx 40rpx rgba(15, 23, 42, 0.08);
}
.wechat-btn {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 700;
  border-radius: 12rpx;
}
.wechat-btn {
  background: #0bb5b1;
}
.hint {
  display: block;
  margin-top: 18rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 36rpx;
  text-align: center;
}
.err {
  display: block;
  margin-top: 22rpx;
  color: #dc2626;
  font-size: 24rpx;
  line-height: 34rpx;
  text-align: center;
}
</style>
