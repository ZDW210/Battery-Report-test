package cn.iocoder.yudao.module.energy.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDevicePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;

import java.util.List;

/**
 * 移动储能设备 Service 接口。
 */
public interface EnergyDeviceService {

    Long createDevice(EnergyDeviceSaveReqVO createReqVO);

    void updateDevice(EnergyDeviceSaveReqVO updateReqVO);

    void deleteDevice(Long id);

    EnergyDeviceDO getDevice(Long id);

    EnergyDeviceDO getDeviceByMeterNo(String meterNo);

    PageResult<EnergyDeviceDO> getDevicePage(EnergyDevicePageReqVO pageReqVO);

    List<EnergyDeviceDO> getDeviceSimpleList(Long customerId, Long projectId);

    EnergyDeviceDO validateDeviceExists(Long id);

}
