package cn.iocoder.yudao.module.energy.service.telemetry;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;

import java.util.List;

public interface EnergyTelemetryService {

    PageResult<EnergyTelemetryDO> getTelemetryPage(EnergyTelemetryPageReqVO pageReqVO);

    List<EnergyTelemetryDO> getTelemetryList(EnergyTelemetryPageReqVO pageReqVO, Integer limit);

    List<EnergyTelemetryDailyStatRespVO> getDailyStatList(EnergyTelemetryDailyStatReqVO reqVO);

}
