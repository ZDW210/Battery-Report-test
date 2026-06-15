export interface ReportArchiveItem {
  id: string
  fileName: string
  fileType: string
  mimeType: string
  size: number
  createdAt: string
  source: string
}

type StoredReportArchiveItem = ReportArchiveItem & { blob: Blob }

const DB_NAME = 'battvest-report-archive'
const STORE_NAME = 'reports'
const DB_VERSION = 1

const canUseIndexedDB = () => typeof window !== 'undefined' && 'indexedDB' in window

const openReportDb = () => {
  return new Promise<IDBDatabase>((resolve, reject) => {
    if (!canUseIndexedDB()) {
      reject(new Error('IndexedDB unavailable'))
      return
    }
    const request = indexedDB.open(DB_NAME, DB_VERSION)
    request.onupgradeneeded = () => {
      const db = request.result
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' })
        store.createIndex('createdAt', 'createdAt')
      }
    }
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

const withStore = async <T>(mode: IDBTransactionMode, handler: (store: IDBObjectStore) => IDBRequest<T>) => {
  const db = await openReportDb()
  return new Promise<T>((resolve, reject) => {
    const transaction = db.transaction(STORE_NAME, mode)
    const store = transaction.objectStore(STORE_NAME)
    const request = handler(store)
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
    transaction.oncomplete = () => db.close()
    transaction.onerror = () => {
      db.close()
      reject(transaction.error)
    }
  })
}

const notifyChanged = () => {
  window.dispatchEvent(new CustomEvent('report-archive-updated'))
}

export const archiveReport = async (fileName: string, blob: Blob, source = '报表导出') => {
  if (!canUseIndexedDB()) return
  const item: StoredReportArchiveItem = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    fileName,
    fileType: fileName.includes('.') ? fileName.split('.').pop() || '' : '',
    mimeType: blob.type || 'application/octet-stream',
    size: blob.size,
    createdAt: new Date().toISOString(),
    source,
    blob
  }
  await withStore('readwrite', (store) => store.put(item))
  notifyChanged()
}

export const listReportArchive = async (): Promise<ReportArchiveItem[]> => {
  if (!canUseIndexedDB()) return []
  const rows = await withStore<StoredReportArchiveItem[]>('readonly', (store) => store.getAll())
  return rows
    .map(({ blob: _blob, ...item }) => item)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}

export const downloadArchivedReport = async (id: string) => {
  const item = await withStore<StoredReportArchiveItem | undefined>('readonly', (store) => store.get(id))
  if (!item) return false
  const url = URL.createObjectURL(item.blob)
  const link = document.createElement('a')
  link.href = url
  link.download = item.fileName
  link.click()
  URL.revokeObjectURL(url)
  return true
}

export const deleteArchivedReport = async (id: string) => {
  await withStore('readwrite', (store) => store.delete(id))
  notifyChanged()
}

export const clearReportArchive = async () => {
  await withStore('readwrite', (store) => store.clear())
  notifyChanged()
}

export const formatReportSize = (size: number) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
