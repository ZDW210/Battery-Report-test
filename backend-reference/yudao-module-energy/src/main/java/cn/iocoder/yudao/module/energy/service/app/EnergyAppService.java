package cn.iocoder.yudao.module.energy.service.app;

import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceRespVO;
import cn.iocoder.yudao.module.energy.controller.app.home.vo.AppEnergyHomeOverviewRespVO;
import cn.iocoder.yudao.module.energy.controller.app.session.vo.AppEnergyScanDeviceRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EnergyAppService {

    AppEnergyHomeOverviewRespVO getHomeOverview();

    List<AppEnergyDeviceRespVO> getDeviceList(AppEnergyDeviceListReqVO reqVO);

    EnergyDeviceDO getAuthorizedDevice(Long deviceId);

    EnergyTelemetryDO getLatestTelemetry(Long deviceId);

    List<EnergyTelemetryDO> getTelemetryList(Long deviceId, LocalDateTime from, LocalDateTime to, Integer limit);

    List<EnergyAlarmDO> getAlarmList(Long deviceId, Integer limit);

    EnergyAlarmDO getAlarm(Long alarmId);

    List<EnergyChargeSessionDO> getChargeSessionList(Long deviceId, Long customerId, Collection<Integer> statuses,
                                                     LocalDateTime from, LocalDateTime to, Integer limit);

    AppEnergyScanDeviceRespVO verifyScanDevice(String scanText);

    AppEnergyScanDeviceRespVO verifyScanDevice(String scanText, String authType, String cardNo);

    EnergyChargeSessionDO startDischargeByScan(String scanText);

    EnergyChargeSessionDO startDischargeByScan(String scanText, String authType, String cardNo);

}
