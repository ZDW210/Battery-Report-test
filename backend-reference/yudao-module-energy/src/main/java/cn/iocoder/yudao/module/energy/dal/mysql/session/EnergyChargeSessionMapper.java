package cn.iocoder.yudao.module.energy.dal.mysql.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnergyChargeSessionMapper extends BaseMapperX<EnergyChargeSessionDO> {

    default PageResult<EnergyChargeSessionDO> selectPage(EnergyChargeSessionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyChargeSessionDO>()
                .likeIfPresent(EnergyChargeSessionDO::getSessionNo, reqVO.getSessionNo())
                .eqIfPresent(EnergyChargeSessionDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyChargeSessionDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyChargeSessionDO::getSessionType, reqVO.getSessionType())
                .eqIfPresent(EnergyChargeSessionDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EnergyChargeSessionDO::getStartTime, reqVO.getStartTime())
                .orderByDesc(EnergyChargeSessionDO::getStartTime)
                .orderByDesc(EnergyChargeSessionDO::getId));
    }

    default EnergyChargeSessionDO selectRunningByDeviceId(Long deviceId) {
        return selectOne(new LambdaQueryWrapperX<EnergyChargeSessionDO>()
                .eq(EnergyChargeSessionDO::getDeviceId, deviceId)
                .eq(EnergyChargeSessionDO::getStatus, 0)
                .last("LIMIT 1"));
    }

}
