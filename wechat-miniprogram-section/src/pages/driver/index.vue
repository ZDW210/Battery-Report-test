<template>
  <view class="page">
    <view class="hero">
      <view class="hero-top">
        <text class="pin">●</text>
        <text class="city">当前位置</text>
      </view>
      <text class="hero-title">移动储能上门服务</text>
      <text class="hero-sub">扫码识别设备，按账户权限启动放电</text>
      <view class="vehicle">
        <view class="vehicle-body">
          <text class="vehicle-text">送电车</text>
        </view>
        <view class="wheel left"></view>
        <view class="wheel right"></view>
      </view>
    </view>

    <view class="service-card">
      <view class="service" @tap="handleScanDischarge">
        <view class="service-icon green">充</view>
        <text class="service-text">放电</text>
      </view>
      <view class="service" @tap="showTodo('救援服务')">
        <view class="service-icon purple">救</view>
        <text class="service-text">救援</text>
      </view>
      <view class="service" @tap="showTodo('搭电服务')">
        <view class="service-icon blue">搭</view>
        <text class="service-text">搭电</text>
      </view>
      <view class="service" @tap="showTodo('换电服务')">
        <view class="service-icon red">换</view>
        <text class="service-text">换电</text>
      </view>
    </view>

    <view class="banner">
      <view>
        <text class="banner-title">扫码放电</text>
        <text class="banner-sub">识别设备后校验微信账号权限</text>
      </view>
      <button class="banner-btn" :loading="scanLoading" @tap="handleScanDischarge">立即扫码</button>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @tap="goOrders">
        <view class="quick-icon">单</view>
        <text>订单</text>
      </view>
      <view class="quick-item" @tap="goMine">
        <view class="quick-icon">账</view>
        <text>账户</text>
      </view>
      <view class="quick-item" @tap="showTodo('服务合作')">
        <view class="quick-icon">合</view>
        <text>合作</text>
      </view>
      <view class="quick-item" @tap="showTodo('渠道加盟')">
        <view class="quick-icon">渠</view>
        <text>渠道</text>
      </view>
    </view>

    <view class="notice-card">
      <view class="notice-head">
        <text class="notice-title">公告列表</text>
        <text class="notice-more">更多</text>
      </view>
      <view class="notice-empty">暂无公告</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '../../stores/auth';
import { sessionApi } from '../../api/sessions';

const auth = useAuthStore();
const scanLoading = ref(false);

function ensureLogin() {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/pages/auth/login' });
    return false;
  }
  return true;
}

function scanCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: true,
      success: (res) => resolve(res.result || ''),
      fail: reject,
    });
  });
}

async function handleScanDischarge() {
  if (!ensureLogin() || scanLoading.value) return;
  scanLoading.value = true;
  try {
    const scanText = await scanCode();
    if (!scanText) {
      uni.showToast({ title: '未识别到二维码内容', icon: 'none' });
      return;
    }
    uni.showLoading({ title: '校验账户...' });
    const scanResult = await sessionApi.verifyScan(scanText);
    uni.hideLoading();
    if (!scanResult.accountKnown) {
      uni.showModal({
        title: '未知账户',
        content: scanResult.message || '当前微信账户未录入或未开放该设备权限，请联系管理员处理。',
        showCancel: false,
        confirmText: '知道了',
      });
      return;
    }
    const deviceName = scanResult.deviceName || scanResult.deviceNo || '扫码设备';
    const content = [
      `账户：${scanResult.accountName || scanResult.accountMobile || scanResult.accountId || '--'}`,
      `设备：${deviceName}`,
      `仪表：${scanResult.meterNo || '--'}`,
      `状态：${scanResult.status === 0 ? '在线' : '非在线'}`,
      '确认后将发起放电任务',
    ].join('\n');
    uni.showModal({
      title: '确认放电',
      content,
      confirmText: '开始放电',
      success: async (res) => {
        if (!res.confirm) return;
        uni.showLoading({ title: '下发放电...' });
        try {
          await sessionApi.startDischargeByScan(scanText);
          uni.showToast({ title: '放电任务已开始', icon: 'success' });
        } catch (error: any) {
          uni.showModal({
            title: '启动失败',
            content: error?.message || error?.msg || error?.data?.msg || '扫码启动失败',
            showCancel: false,
            confirmText: '知道了',
          });
        } finally {
          uni.hideLoading();
        }
      },
    });
  } catch (error: any) {
    uni.hideLoading();
    if (!error?.errMsg?.includes('cancel')) {
      uni.showToast({ title: error?.message || '扫码启动失败', icon: 'none' });
    }
  } finally {
    scanLoading.value = false;
  }
}

function goOrders() {
  uni.switchTab({ url: '/pages/orders/index' });
}

function goMine() {
  uni.switchTab({ url: '/pages/mine/index' });
}

function showTodo(name: string) {
  uni.showToast({ title: `${name}建设中`, icon: 'none' });
}

onShow(() => {
  ensureLogin();
});

onPullDownRefresh(() => {
  uni.stopPullDownRefresh();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 28rpx 40rpx;
  background: #f3f4f6;
}
.hero {
  position: relative;
  min-height: 300rpx;
  margin: 0 -28rpx;
  padding: calc(var(--status-bar-height) + 32rpx) 32rpx 24rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #31e6a2 0%, #77e6bd 52%, #b7f4dd 100%);
}
.hero::before {
  position: absolute;
  top: -90rpx;
  left: -80rpx;
  width: 300rpx;
  height: 300rpx;
  content: '';
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}
.hero-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #052e26;
  font-size: 24rpx;
}
.pin {
  color: #042f2e;
  font-size: 24rpx;
}
.hero-title {
  position: relative;
  display: block;
  margin-top: 54rpx;
  color: #06130f;
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1.1;
}
.hero-sub {
  position: relative;
  display: block;
  margin-top: 14rpx;
  color: rgba(6, 19, 15, 0.62);
  font-size: 26rpx;
}
.vehicle {
  position: absolute;
  right: 46rpx;
  bottom: 26rpx;
  width: 300rpx;
  height: 116rpx;
}
.vehicle-body {
  position: absolute;
  right: 0;
  bottom: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 286rpx;
  height: 74rpx;
  border-radius: 34rpx 56rpx 28rpx 26rpx;
  background: linear-gradient(180deg, #f8fafc, #cbd5e1);
  border: 3rpx solid rgba(15, 23, 42, 0.16);
  box-shadow: 0 14rpx 20rpx rgba(15, 23, 42, 0.16);
}
.vehicle-text {
  color: #475569;
  font-size: 28rpx;
  font-weight: 800;
}
.wheel {
  position: absolute;
  bottom: 10rpx;
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  background: #0f172a;
  border: 8rpx solid #64748b;
}
.wheel.left { left: 64rpx; }
.wheel.right { right: 48rpx; }
.service-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  margin-top: -24rpx;
  padding: 34rpx 18rpx 30rpx;
  border-radius: 18rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 34rpx rgba(15, 23, 42, 0.08);
}
.service {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
}
.service-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: 0 10rpx 20rpx rgba(15, 23, 42, 0.14);
}
.service-icon.green { background: linear-gradient(135deg, #16a34a, #22c55e); }
.service-icon.purple { background: linear-gradient(135deg, #a855f7, #c084fc); }
.service-icon.blue { background: linear-gradient(135deg, #4f7cff, #93a8ff); }
.service-icon.red { background: linear-gradient(135deg, #fb7185, #fb8a80); }
.service-text {
  color: #111827;
  font-size: 26rpx;
  font-weight: 600;
}
.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding: 30rpx 26rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #16e69d, #2fd59c);
}
.banner-title {
  display: block;
  color: #06231b;
  font-size: 38rpx;
  font-weight: 900;
}
.banner-sub {
  display: block;
  margin-top: 6rpx;
  color: #06352a;
  font-size: 24rpx;
}
.banner-btn {
  margin: 0;
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  color: #16b981;
  font-size: 24rpx;
  font-weight: 800;
  border-radius: 32rpx;
  background: #ffffff;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18rpx;
  margin-top: 24rpx;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 26rpx 8rpx 22rpx;
  border-radius: 18rpx;
  background: #ffffff;
  color: #111827;
  font-size: 22rpx;
}
.quick-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  border-radius: 16rpx;
  color: #10b981;
  font-size: 26rpx;
  font-weight: 900;
  background: #d1fae5;
}
.notice-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #ffffff;
}
.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.notice-title {
  color: #030712;
  font-size: 36rpx;
  font-weight: 900;
}
.notice-more {
  color: #6b7280;
  font-size: 24rpx;
}
.notice-empty {
  padding: 30rpx 0 10rpx;
  color: #9ca3af;
  font-size: 24rpx;
}
</style>
