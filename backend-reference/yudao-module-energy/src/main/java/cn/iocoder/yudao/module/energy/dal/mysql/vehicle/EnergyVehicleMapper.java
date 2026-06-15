package cn.iocoder.yudao.module.energy.dal.mysql.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo.EnergyVehiclePageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.vehicle.EnergyVehicleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnergyVehicleMapper extends BaseMapperX<EnergyVehicleDO> {

    default PageResult<EnergyVehicleDO> selectPage(EnergyVehiclePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyVehicleDO>()
                .likeIfPresent(EnergyVehicleDO::getVehicleNo, reqVO.getVehicleNo())
                .likeIfPresent(EnergyVehicleDO::getPlateNo, reqVO.getPlateNo())
                .likeIfPresent(EnergyVehicleDO::getQrCode, reqVO.getQrCode())
                .eqIfPresent(EnergyVehicleDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyVehicleDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyVehicleDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(EnergyVehicleDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyVehicleDO::getId));
    }

    default EnergyVehicleDO selectByQrCode(String qrCode) {
        return selectOne(new LambdaQueryWrapperX<EnergyVehicleDO>()
                .eq(EnergyVehicleDO::getQrCode, qrCode)
                .eq(EnergyVehicleDO::getStatus, 0));
    }

    default EnergyVehicleDO selectByVehicleNo(String vehicleNo) {
        return selectOne(new LambdaQueryWrapperX<EnergyVehicleDO>()
                .eq(EnergyVehicleDO::getVehicleNo, vehicleNo)
                .eq(EnergyVehicleDO::getStatus, 0));
    }

    default EnergyVehicleDO selectByVehicleNoIgnoreStatus(String vehicleNo) {
        return selectOne(EnergyVehicleDO::getVehicleNo, vehicleNo);
    }

    default EnergyVehicleDO selectByQrCodeIgnoreStatus(String qrCode) {
        return selectOne(EnergyVehicleDO::getQrCode, qrCode);
    }

    default EnergyVehicleDO selectByPlateNo(String plateNo) {
        return selectOne(new LambdaQueryWrapperX<EnergyVehicleDO>()
                .eq(EnergyVehicleDO::getPlateNo, plateNo)
                .eq(EnergyVehicleDO::getStatus, 0));
    }

    default EnergyVehicleDO selectFirstByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<EnergyVehicleDO>()
                .and(query -> query
                        .eq(EnergyVehicleDO::getQrCode, code)
                        .or()
                        .eq(EnergyVehicleDO::getVehicleNo, code)
                        .or()
                        .eq(EnergyVehicleDO::getPlateNo, code))
                .eq(EnergyVehicleDO::getStatus, 0)
                .last("LIMIT 1"));
    }

}
