package cn.iocoder.yudao.module.energy.service.device;

import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlRespVO;

public interface EnergyDeviceControlService {

    EnergyDeviceControlRespVO controlDevice(EnergyDeviceControlReqVO reqVO, Long operatorUserId);

}
