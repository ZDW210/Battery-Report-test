export function formatNumber(val: number | null | undefined, decimals = 2): string {
  if (val == null) return '--';
  return Number(val).toFixed(decimals);
}

export function formatTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '--';
  const d = new Date(dateStr);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatTimeAgo(dateStr: string | null | undefined): string {
  if (!dateStr) return '--';
  const now = Date.now();
  const diff = now - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  return `${days}天前`;
}
