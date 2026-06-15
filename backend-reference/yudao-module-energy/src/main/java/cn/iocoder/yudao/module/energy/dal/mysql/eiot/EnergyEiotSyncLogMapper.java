package cn.iocoder.yudao.module.energy.dal.mysql.eiot;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.eiot.vo.EnergyEiotSyncLogPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.eiot.EnergyEiotSyncLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 移动储能 EIOT 同步日志 Mapper。
 */
@Mapper
public interface EnergyEiotSyncLogMapper extends BaseMapperX<EnergyEiotSyncLogDO> {

    default PageResult<EnergyEiotSyncLogDO> selectPage(EnergyEiotSyncLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyEiotSyncLogDO>()
                .eqIfPresent(EnergyEiotSyncLogDO::getSyncType, reqVO.getSyncType())
                .eqIfPresent(EnergyEiotSyncLogDO::getStatus, reqVO.getStatus())
                .likeIfPresent(EnergyEiotSyncLogDO::getRequestId, reqVO.getRequestId())
                .likeIfPresent(EnergyEiotSyncLogDO::getGatewaySn, reqVO.getGatewaySn())
                .likeIfPresent(EnergyEiotSyncLogDO::getMeterSn, reqVO.getMeterSn())
                .betweenIfPresent(EnergyEiotSyncLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnergyEiotSyncLogDO::getCreateTime)
                .orderByDesc(EnergyEiotSyncLogDO::getId));
    }

}
