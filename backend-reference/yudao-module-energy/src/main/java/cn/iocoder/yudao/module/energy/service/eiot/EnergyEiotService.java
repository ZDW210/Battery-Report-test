package cn.iocoder.yudao.module.energy.service.eiot;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.eiot.vo.EnergyEiotSyncLogPageReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmRespVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.eiot.EnergyEiotSyncLogDO;

/**
 * 移动储能 EIOT 接入 Service 接口。
 */
public interface EnergyEiotService {

    EnergyEiotAlarmRespVO receiveAlarm(EnergyEiotAlarmReqVO reqVO);

    EnergyEiotRealtimeRespVO receiveRealtime(EnergyEiotRealtimeReqVO reqVO);

    PageResult<EnergyEiotSyncLogDO> getSyncLogPage(EnergyEiotSyncLogPageReqVO pageReqVO);

}
