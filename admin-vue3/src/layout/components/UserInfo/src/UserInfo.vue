<script lang="ts" setup>
import { ElMessageBox } from 'element-plus'

import avatarImg from '@/assets/imgs/battvest-avatar.svg'
import { useDesign } from '@/hooks/web/useDesign'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useUserStore } from '@/store/modules/user'
import LockDialog from './components/LockDialog.vue'
import LockPage from './components/LockPage.vue'
import { useLockStore } from '@/store/modules/lock'

defineOptions({ name: 'UserInfo' })

const { t } = useI18n()

const { push, replace } = useRouter()

const userStore = useUserStore()

const tagsViewStore = useTagsViewStore()

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('user-info')

const avatar = computed(() => userStore.user.avatar || avatarImg)
const userName = computed(() => userStore.user.nickname ?? 'Admin')
const roleName = computed(() => (userStore.roles.includes('admin') ? '系统管理员' : '客户账号'))

// 锁定屏幕
const lockStore = useLockStore()
const getIsLock = computed(() => lockStore.getLockInfo?.isLock ?? false)
const dialogVisible = ref<boolean>(false)
const lockScreen = () => {
  dialogVisible.value = true
}

const loginOut = async () => {
  try {
    await ElMessageBox.confirm(t('common.loginOutMessage'), t('common.reminder'), {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await userStore.loginOut()
    tagsViewStore.delAllViews()
    replace('/login?redirect=/index')
  } catch {}
}
const toProfile = async () => {
  push('/user/profile')
}
const toPassword = async () => {
  push('/user/profile?tab=resetPwd')
}
const refreshPermission = async () => {
  await userStore.setUserInfoAction()
}
const toExportRecords = () => {
  push('/report/export-records')
}
const toDownloadCenter = () => {
  push('/report/download-center')
}
</script>

<template>
  <ElDropdown class="custom-hover" :class="prefixCls" trigger="click">
    <div class="flex items-center">
      <ElAvatar :src="avatar" alt="" class="battvest-avatar" />
      <span class="pl-[5px] text-14px text-[var(--top-header-text-color)] <lg:hidden">
        {{ userName }}
      </span>
    </div>
    <template #dropdown>
      <ElDropdownMenu class="battvest-user-menu">
        <div class="battvest-user-card">
          <img :src="avatar" alt="" class="battvest-user-logo" />
          <div class="battvest-user-name">{{ userName }}</div>
          <div class="battvest-user-role">{{ roleName }}</div>
        </div>
        <ElDropdownItem @click="toProfile">
          <Icon icon="ep:user" />
          <div>{{ t('common.profile') }}</div>
        </ElDropdownItem>
        <ElDropdownItem @click="refreshPermission">
          <Icon icon="ep:refresh" />
          <div>刷新权限</div>
        </ElDropdownItem>
        <ElDropdownItem @click="toPassword">
          <Icon icon="ep:question-filled" />
          <div>修改密码</div>
        </ElDropdownItem>
        <ElDropdownItem @click="toExportRecords">
          <Icon icon="ep:document" />
          <div>导出记录</div>
        </ElDropdownItem>
        <ElDropdownItem @click="toDownloadCenter">
          <Icon icon="ep:download" />
          <div>下载中心</div>
        </ElDropdownItem>
        <ElDropdownItem divided>
          <Icon icon="ep:lock" />
          <div @click="lockScreen">{{ t('lock.lockScreen') }}</div>
        </ElDropdownItem>
        <ElDropdownItem divided @click="loginOut">
          <Icon icon="ep:switch-button" />
          <div>{{ t('common.loginOut') }}</div>
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>

  <LockDialog v-if="dialogVisible" v-model="dialogVisible" />

  <teleport to="body">
    <transition name="fade-bottom" mode="out-in">
      <LockPage v-if="getIsLock" />
    </transition>
  </teleport>
</template>

<style scoped lang="scss">
.fade-bottom-enter-active,
.fade-bottom-leave-active {
  transition:
    opacity 0.25s,
    transform 0.3s;
}

.fade-bottom-enter-from {
  opacity: 0;
  transform: translateY(-10%);
}

.fade-bottom-leave-to {
  opacity: 0;
  transform: translateY(10%);
}

.battvest-avatar {
  width: 42px;
  height: 42px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
}

.battvest-user-menu {
  min-width: 280px;
}

.battvest-user-card {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 18px 18px 16px;
  border-bottom: 1px solid #edf2f7;
}

.battvest-user-logo {
  width: 64px;
  height: 64px;
}

.battvest-user-name {
  color: #1f2937;
  font-size: 15px;
}

.battvest-user-role {
  padding: 6px 10px;
  border-radius: 6px;
  color: #22c55e;
  background: #ecfdf5;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

:deep(.battvest-user-menu .el-dropdown-menu__item) {
  gap: 12px;
  padding: 12px 20px;
  color: #111827;
  font-size: 15px;
}
</style>
