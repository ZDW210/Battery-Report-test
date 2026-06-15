<template>
  <ContentWrap>
    <div class="archive-toolbar">
      <div>
        <div class="archive-title">{{ title }}</div>
        <div class="archive-count">共 {{ list.length }} 个文件</div>
      </div>
      <div class="archive-actions">
        <el-button @click="loadList"><Icon class="mr-5px" icon="ep:refresh" />刷新</el-button>
        <el-button :disabled="!list.length" type="danger" plain @click="handleClear">
          <Icon class="mr-5px" icon="ep:delete" />清空
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="文件名称" min-width="260" prop="fileName" />
      <el-table-column align="center" label="来源" min-width="120" prop="source" />
      <el-table-column align="center" label="类型" width="90" prop="fileType">
        <template #default="{ row }">{{ row.fileType || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="大小" width="110">
        <template #default="{ row }">{{ formatReportSize(row.size) }}</template>
      </el-table-column>
      <el-table-column align="center" label="导出时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDownload(row.id)">下载</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import {
  clearReportArchive,
  deleteArchivedReport,
  downloadArchivedReport,
  formatReportSize,
  listReportArchive,
  type ReportArchiveItem
} from '@/utils/reportArchive'

defineOptions({ name: 'ReportArchiveList' })

defineProps<{ title: string }>()

const message = useMessage()
const loading = ref(false)
const list = ref<ReportArchiveItem[]>([])

const loadList = async () => {
  loading.value = true
  try {
    list.value = await listReportArchive()
  } finally {
    loading.value = false
  }
}

const formatDate = (value: string) => dayjs(value).format('YYYY-MM-DD HH:mm:ss')

const handleDownload = async (id: string) => {
  const ok = await downloadArchivedReport(id)
  if (!ok) message.warning('文件不存在')
}

const handleDelete = async (id: string) => {
  await deleteArchivedReport(id)
  await loadList()
}

const handleClear = async () => {
  try {
    await message.delConfirm()
    await clearReportArchive()
    await loadList()
  } catch {}
}

onMounted(() => {
  loadList()
  window.addEventListener('report-archive-updated', loadList)
})

onBeforeUnmount(() => {
  window.removeEventListener('report-archive-updated', loadList)
})
</script>

<style scoped lang="scss">
.archive-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.archive-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.archive-count {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.archive-actions {
  display: flex;
  gap: 8px;
}
</style>
