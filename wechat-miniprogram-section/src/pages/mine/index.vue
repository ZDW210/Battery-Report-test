<template>
  <view class="page">
    <view class="profile-card">
      <view class="avatar">{{ avatarText }}</view>
      <view class="profile-main">
        <text class="name">{{ auth.user?.displayName || '微信用户' }}</text>
        <text class="role">{{ auth.user?.miniAdminEnabled ? '已开放管理权限' : '司机端账号' }}</text>
      </view>
      <button class="refresh-btn" :loading="refreshing" @tap="refreshProfile">刷新</button>
    </view>

    <view class="menu-card">
      <view class="menu-item" @tap="goOrders">
        <view class="menu-icon">单</view>
        <view class="menu-main">
          <text class="menu-title">我的订单</text>
          <text class="menu-sub">查看扫码放电和计费记录</text>
        </view>
        <text class="arrow">></text>
      </view>
      <view v-if="auth.user?.miniAdminEnabled" class="menu-item" @tap="goManager">
        <view class="menu-icon manage">管</view>
        <view class="menu-main">
          <text class="menu-title">管理</text>
          <text class="menu-sub">进入设备、报表和数据导出页面</text>
        </view>
        <text class="arrow">></text>
      </view>
      <view v-else class="locked">
        <text class="locked-title">管理入口未开放</text>
        <text class="locked-sub">首次微信登录后账号已自动录入后台，需要运营人员在网页端“使用账户”中开放小程序管理权限。</text>
      </view>
    </view>

    <view class="menu-card">
      <view class="info-row">
        <text>账号</text>
        <text>{{ auth.user?.username || '--' }}</text>
      </view>
      <view class="info-row">
        <text>用户编号</text>
        <text>{{ auth.user?.id || '--' }}</text>
      </view>
    </view>

    <button class="logout" @tap="logout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '../../stores/auth';

const auth = useAuthStore();
const refreshing = ref(false);

const avatarText = computed(() => {
  const name = auth.user?.displayName || auth.user?.username || '微';
  return name.slice(0, 1);
});

function ensureLogin() {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/pages/auth/login' });
    return false;
  }
  return true;
}

async function refreshProfile() {
  if (!ensureLogin()) return;
  refreshing.value = true;
  try {
    await auth.refreshProfile();
  } catch (error: any) {
    uni.showToast({ title: error?.message || '刷新失败', icon: 'none' });
  } finally {
    refreshing.value = false;
    uni.stopPullDownRefresh();
  }
}

function goOrders() {
  uni.switchTab({ url: '/pages/orders/index' });
}

function goManager() {
  uni.navigateTo({ url: '/pages/index/index' });
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前微信账号吗？',
    success: (res) => {
      if (res.confirm) auth.logout();
    },
  });
}

onShow(() => {
  if (ensureLogin()) refreshProfile();
});
onPullDownRefresh(refreshProfile);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f3f4f6;
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #10b981, #34d399);
}
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  color: #10b981;
  font-size: 40rpx;
  font-weight: 900;
  background: #ffffff;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.name {
  display: block;
  overflow: hidden;
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.role {
  display: block;
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
}
.refresh-btn {
  margin: 0;
  width: 112rpx;
  height: 56rpx;
  line-height: 56rpx;
  color: #047857;
  font-size: 22rpx;
  border-radius: 28rpx;
  background: #ffffff;
}
.menu-card {
  margin-top: 22rpx;
  padding: 8rpx 24rpx;
  border-radius: 18rpx;
  background: #ffffff;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 24rpx 0;
}
.menu-item:not(:last-child) {
  border-bottom: 1rpx solid #eef2f7;
}
.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  color: #10b981;
  font-size: 28rpx;
  font-weight: 900;
  background: #d1fae5;
}
.menu-icon.manage {
  color: #2563eb;
  background: #dbeafe;
}
.menu-main {
  flex: 1;
  min-width: 0;
}
.menu-title {
  display: block;
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
}
.menu-sub {
  display: block;
  margin-top: 6rpx;
  color: #9ca3af;
  font-size: 22rpx;
}
.arrow {
  color: #9ca3af;
  font-size: 32rpx;
}
.locked {
  padding: 24rpx 0;
  border-top: 1rpx solid #eef2f7;
}
.locked-title {
  display: block;
  color: #6b7280;
  font-size: 26rpx;
  font-weight: 800;
}
.locked-sub {
  display: block;
  margin-top: 8rpx;
  color: #9ca3af;
  font-size: 22rpx;
  line-height: 34rpx;
}
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 22rpx 0;
  color: #6b7280;
  font-size: 24rpx;
}
.info-row:not(:last-child) {
  border-bottom: 1rpx solid #eef2f7;
}
.info-row text:last-child {
  max-width: 460rpx;
  overflow: hidden;
  color: #111827;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.logout {
  margin-top: 30rpx;
  color: #ef4444;
  font-size: 28rpx;
  border-radius: 14rpx;
  background: #ffffff;
}
</style>
