export function downloadAndOpenExcel(base64Data: string, filename: string): void {
  // @ts-ignore
  const fs = wx.getFileSystemManager();
  // @ts-ignore
  const filePath = `${wx.env.USER_DATA_PATH}/${filename}`;
  fs.writeFile({
    filePath,
    data: base64Data,
    encoding: 'base64',
    success() {
      uni.openDocument({
        filePath,
        showMenu: true,
        fail() {
          uni.showToast({ title: '无法打开文件', icon: 'none' });
        },
      });
    },
    fail() {
      uni.showToast({ title: '文件写入失败', icon: 'none' });
    },
  });
}
