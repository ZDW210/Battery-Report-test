package cn.iocoder.yudao.module.energy.dal.mysql.telemetry;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnergyTelemetryMapper extends BaseMapperX<EnergyTelemetryDO> {

    default PageResult<EnergyTelemetryDO> selectPage(EnergyTelemetryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyTelemetryDO>()
                .eqIfPresent(EnergyTelemetryDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyTelemetryDO::getGatewaySn, reqVO.getGatewaySn())
                .eqIfPresent(EnergyTelemetryDO::getMeterSn, reqVO.getMeterSn())
                .eqIfPresent(EnergyTelemetryDO::getMeterNo, reqVO.getMeterNo())
                .eqIfPresent(EnergyTelemetryDO::getSource, reqVO.getSource())
                .eqIfPresent(EnergyTelemetryDO::getState, reqVO.getState())
                .betweenIfPresent(EnergyTelemetryDO::getCollectTime, reqVO.getCollectTime())
                .orderByDesc(EnergyTelemetryDO::getCollectTime)
                .orderByDesc(EnergyTelemetryDO::getId));
    }

    default List<EnergyTelemetryDO> selectList(EnergyTelemetryPageReqVO reqVO, Integer limit) {
        LambdaQueryWrapper<EnergyTelemetryDO> wrapper = new LambdaQueryWrapperX<EnergyTelemetryDO>()
                .eqIfPresent(EnergyTelemetryDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyTelemetryDO::getGatewaySn, reqVO.getGatewaySn())
                .eqIfPresent(EnergyTelemetryDO::getMeterSn, reqVO.getMeterSn())
                .eqIfPresent(EnergyTelemetryDO::getMeterNo, reqVO.getMeterNo())
                .eqIfPresent(EnergyTelemetryDO::getSource, reqVO.getSource())
                .eqIfPresent(EnergyTelemetryDO::getState, reqVO.getState())
                .betweenIfPresent(EnergyTelemetryDO::getCollectTime, reqVO.getCollectTime())
                .orderByAsc(EnergyTelemetryDO::getCollectTime)
                .orderByAsc(EnergyTelemetryDO::getId);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + Math.min(limit, 5000));
        }
        return selectList(wrapper);
    }

}
