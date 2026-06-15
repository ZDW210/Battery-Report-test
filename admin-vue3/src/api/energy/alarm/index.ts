import request from '@/config/axios'

// 移动储能告警 VO
export interface EnergyAlarmVO {
  id?: number
  alarmNo?: string
  deviceId?: number
  deviceName?: string
  code?: string
  level?: number
  title?: string
  content?: string
  status?: number
  occurTime?: Date
  ackUserId?: number
  ackTime?: Date
  closeTime?: Date
  createTime?: Date
}

export interface EnergyAlarmHandleVO {
  id: number
  remark?: string
}

// 移动储能告警 API
export const EnergyAlarmApi = {
  // 查询告警分页
  getAlarmPage: async (params: any) => {
    return await request.get({ url: '/energy/alarm/page', params })
  },

  // 查询告警详情
  getAlarm: async (id: number) => {
    return await request.get({ url: '/energy/alarm/get?id=' + id })
  },

  // 确认告警
  ackAlarm: async (data: EnergyAlarmHandleVO) => {
    return await request.put({ url: '/energy/alarm/ack', data })
  },

  // 关闭告警
  closeAlarm: async (data: EnergyAlarmHandleVO) => {
    return await request.put({ url: '/energy/alarm/close', data })
  }
}
