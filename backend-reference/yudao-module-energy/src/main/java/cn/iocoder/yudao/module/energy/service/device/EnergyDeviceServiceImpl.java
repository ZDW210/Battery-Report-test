package cn.iocoder.yudao.module.energy.service.device;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDevicePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.mysql.device.EnergyDeviceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

/**
 * 移动储能设备 Service 实现类。
 */
@Service
@Validated
public class EnergyDeviceServiceImpl implements EnergyDeviceService {

    @Resource
    private EnergyDeviceMapper deviceMapper;

    @Override
    public Long createDevice(EnergyDeviceSaveReqVO createReqVO) {
        validateDeviceSaveData(createReqVO);

        EnergyDeviceDO device = BeanUtils.toBean(createReqVO, EnergyDeviceDO.class);
        deviceMapper.insert(device);
        return device.getId();
    }

    @Override
    public void updateDevice(EnergyDeviceSaveReqVO updateReqVO) {
        validateDeviceExists(updateReqVO.getId());
        validateDeviceSaveData(updateReqVO);

        EnergyDeviceDO updateObj = BeanUtils.toBean(updateReqVO, EnergyDeviceDO.class);
        deviceMapper.updateById(updateObj);
    }

    private void validateDeviceSaveData(EnergyDeviceSaveReqVO reqVO) {
        validateDeviceNoUnique(reqVO.getId(), reqVO.getDeviceNo());
        validateMeterNoUnique(reqVO.getId(), reqVO.getMeterNo());
    }

    private void validateDeviceNoUnique(Long id, String deviceNo) {
        EnergyDeviceDO device = deviceMapper.selectByDeviceNo(deviceNo);
        if (device == null) {
            return;
        }
        if (ObjUtil.notEqual(device.getId(), id)) {
            throw exception(DEVICE_NO_DUPLICATE);
        }
    }

    private void validateMeterNoUnique(Long id, String meterNo) {
        if (StrUtil.isBlank(meterNo)) {
            return;
        }
        EnergyDeviceDO device = deviceMapper.selectByMeterNo(meterNo);
        if (device == null) {
            return;
        }
        if (ObjUtil.notEqual(device.getId(), id)) {
            throw exception(DEVICE_METER_NO_DUPLICATE);
        }
    }

    @Override
    public void deleteDevice(Long id) {
        validateDeviceExists(id);
        deviceMapper.deleteById(id);
    }

    @Override
    public EnergyDeviceDO getDevice(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    public EnergyDeviceDO getDeviceByMeterNo(String meterNo) {
        return deviceMapper.selectByMeterNo(meterNo);
    }

    @Override
    public PageResult<EnergyDeviceDO> getDevicePage(EnergyDevicePageReqVO pageReqVO) {
        return deviceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EnergyDeviceDO> getDeviceSimpleList(Long customerId, Long projectId) {
        return deviceMapper.selectSimpleList(customerId, projectId);
    }

    @Override
    public EnergyDeviceDO validateDeviceExists(Long id) {
        EnergyDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

}
