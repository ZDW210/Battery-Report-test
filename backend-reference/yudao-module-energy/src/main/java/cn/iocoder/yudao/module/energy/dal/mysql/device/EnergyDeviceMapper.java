package cn.iocoder.yudao.module.energy.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDevicePageReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 移动储能设备 Mapper。
 */
@Mapper
public interface EnergyDeviceMapper extends BaseMapperX<EnergyDeviceDO> {

    default PageResult<EnergyDeviceDO> selectPage(EnergyDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyDeviceDO>()
                .eqIfPresent(EnergyDeviceDO::getDeviceNo, reqVO.getDeviceNo())
                .likeIfPresent(EnergyDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(EnergyDeviceDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(EnergyDeviceDO::getGatewaySn, reqVO.getGatewaySn())
                .eqIfPresent(EnergyDeviceDO::getMeterSn, reqVO.getMeterSn())
                .eqIfPresent(EnergyDeviceDO::getMeterNo, reqVO.getMeterNo())
                .eqIfPresent(EnergyDeviceDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyDeviceDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(EnergyDeviceDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyDeviceDO::getId));
    }

    default EnergyDeviceDO selectByDeviceNo(String deviceNo) {
        return selectOne(EnergyDeviceDO::getDeviceNo, deviceNo);
    }

    default EnergyDeviceDO selectByMeterNo(String meterNo) {
        return selectOne(EnergyDeviceDO::getMeterNo, meterNo);
    }

    default EnergyDeviceDO selectByGatewaySnAndMeterSn(String gatewaySn, String meterSn) {
        return selectOne(new LambdaQueryWrapperX<EnergyDeviceDO>()
                .eq(EnergyDeviceDO::getGatewaySn, gatewaySn)
                .eq(EnergyDeviceDO::getMeterSn, meterSn));
    }

    default List<EnergyDeviceDO> selectSimpleList(Long customerId, Long projectId) {
        return selectList(new LambdaQueryWrapperX<EnergyDeviceDO>()
                .eqIfPresent(EnergyDeviceDO::getCustomerId, customerId)
                .eqIfPresent(EnergyDeviceDO::getProjectId, projectId)
                .orderByDesc(EnergyDeviceDO::getId));
    }

    default List<EnergyDeviceDO> selectAppList(AppEnergyDeviceListReqVO reqVO) {
        return selectAppList(reqVO, null, null, null);
    }

    default List<EnergyDeviceDO> selectAppList(AppEnergyDeviceListReqVO reqVO,
                                               Collection<Long> customerIds,
                                               Collection<Long> projectIds,
                                               Collection<Long> deviceIds) {
        LambdaQueryWrapperX<EnergyDeviceDO> wrapper = new LambdaQueryWrapperX<EnergyDeviceDO>()
                .eqIfPresent(EnergyDeviceDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyDeviceDO::getId)
                .last("LIMIT 100");
        boolean hasScope = (deviceIds != null && !deviceIds.isEmpty())
                || (projectIds != null && !projectIds.isEmpty())
                || (customerIds != null && !customerIds.isEmpty());
        if (hasScope) {
            wrapper.and(query -> {
                boolean hasPrevious = false;
                if (deviceIds != null && !deviceIds.isEmpty()) {
                    query.in(EnergyDeviceDO::getId, deviceIds);
                    hasPrevious = true;
                }
                if (projectIds != null && !projectIds.isEmpty()) {
                    if (hasPrevious) {
                        query.or();
                    }
                    query.in(EnergyDeviceDO::getProjectId, projectIds);
                    hasPrevious = true;
                }
                if (customerIds != null && !customerIds.isEmpty()) {
                    if (hasPrevious) {
                        query.or();
                    }
                    query.in(EnergyDeviceDO::getCustomerId, customerIds);
                }
            });
        }
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(query -> query
                    .like(EnergyDeviceDO::getDeviceName, reqVO.getKeyword())
                    .or()
                    .like(EnergyDeviceDO::getDeviceNo, reqVO.getKeyword())
                    .or()
                    .like(EnergyDeviceDO::getMeterNo, reqVO.getKeyword())
                    .or()
                    .like(EnergyDeviceDO::getGatewaySn, reqVO.getKeyword())
                    .or()
                    .like(EnergyDeviceDO::getMeterSn, reqVO.getKeyword()));
        }
        return selectList(wrapper);
    }

}
