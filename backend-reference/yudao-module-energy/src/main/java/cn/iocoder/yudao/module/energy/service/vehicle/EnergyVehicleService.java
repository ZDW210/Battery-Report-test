package cn.iocoder.yudao.module.energy.service.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehiclePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehicleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.vehicle.EnergyVehicleDO;

public interface EnergyVehicleService {

    Long createVehicle(EnergyVehicleSaveReqVO createReqVO);

    void updateVehicle(EnergyVehicleSaveReqVO updateReqVO);

    void deleteVehicle(Long id);

    EnergyVehicleDO getVehicle(Long id);

    PageResult<EnergyVehicleDO> getVehiclePage(EnergyVehiclePageReqVO pageReqVO);

    EnergyVehicleDO validateVehicleExists(Long id);

}
