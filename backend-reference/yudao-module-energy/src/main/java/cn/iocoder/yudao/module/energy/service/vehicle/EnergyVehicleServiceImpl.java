package cn.iocoder.yudao.module.energy.service.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehiclePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehicleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.vehicle.EnergyVehicleDO;
import cn.iocoder.yudao.module.energy.dal.mysql.vehicle.EnergyVehicleMapper;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.VEHICLE_NOT_EXISTS;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.VEHICLE_NO_DUPLICATE;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.VEHICLE_QR_CODE_DUPLICATE;

@Service
@Validated
public class EnergyVehicleServiceImpl implements EnergyVehicleService {

    @Resource
    private EnergyVehicleMapper vehicleMapper;
    @Resource
    private EnergyDeviceService deviceService;

    @Override
    public Long createVehicle(EnergyVehicleSaveReqVO createReqVO) {
        EnergyDeviceDO device = validateVehicleSaveData(null, createReqVO);
        EnergyVehicleDO vehicle = BeanUtils.toBean(createReqVO, EnergyVehicleDO.class);
        fillScopeFromDevice(vehicle, device);
        vehicleMapper.insert(vehicle);
        return vehicle.getId();
    }

    @Override
    public void updateVehicle(EnergyVehicleSaveReqVO updateReqVO) {
        validateVehicleExists(updateReqVO.getId());
        EnergyDeviceDO device = validateVehicleSaveData(updateReqVO.getId(), updateReqVO);
        EnergyVehicleDO updateObj = BeanUtils.toBean(updateReqVO, EnergyVehicleDO.class);
        fillScopeFromDevice(updateObj, device);
        vehicleMapper.updateById(updateObj);
    }

    @Override
    public void deleteVehicle(Long id) {
        validateVehicleExists(id);
        vehicleMapper.deleteById(id);
    }

    @Override
    public EnergyVehicleDO getVehicle(Long id) {
        return vehicleMapper.selectById(id);
    }

    @Override
    public PageResult<EnergyVehicleDO> getVehiclePage(EnergyVehiclePageReqVO pageReqVO) {
        return vehicleMapper.selectPage(pageReqVO);
    }

    @Override
    public EnergyVehicleDO validateVehicleExists(Long id) {
        EnergyVehicleDO vehicle = vehicleMapper.selectById(id);
        if (vehicle == null) {
            throw exception(VEHICLE_NOT_EXISTS);
        }
        return vehicle;
    }

    private EnergyDeviceDO validateVehicleSaveData(Long id, EnergyVehicleSaveReqVO reqVO) {
        EnergyVehicleDO sameVehicleNo = vehicleMapper.selectByVehicleNoIgnoreStatus(reqVO.getVehicleNo());
        if (sameVehicleNo != null && !sameVehicleNo.getId().equals(id)) {
            throw exception(VEHICLE_NO_DUPLICATE);
        }
        EnergyVehicleDO sameQrCode = vehicleMapper.selectByQrCodeIgnoreStatus(reqVO.getQrCode());
        if (sameQrCode != null && !sameQrCode.getId().equals(id)) {
            throw exception(VEHICLE_QR_CODE_DUPLICATE);
        }
        return deviceService.validateDeviceExists(reqVO.getDeviceId());
    }

    private void fillScopeFromDevice(EnergyVehicleDO vehicle, EnergyDeviceDO device) {
        vehicle.setCustomerId(device.getCustomerId());
        vehicle.setProjectId(device.getProjectId());
    }

}
