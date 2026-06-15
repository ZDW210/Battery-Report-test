package cn.iocoder.yudao.module.energy.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;

/**
 * 移动储能告警 Service 接口。
 */
public interface EnergyAlarmService {

    PageResult<EnergyAlarmDO> getAlarmPage(EnergyAlarmPageReqVO pageReqVO);

    EnergyAlarmDO getAlarm(Long id);

    void ackAlarm(Long id, Long userId, String remark);

    void closeAlarm(Long id, Long userId, String remark);

    EnergyAlarmDO getAlarmByAlarmNo(String alarmNo);

    Long createAlarm(EnergyAlarmDO alarm);

    EnergyAlarmDO validateAlarmExists(Long id);

}
