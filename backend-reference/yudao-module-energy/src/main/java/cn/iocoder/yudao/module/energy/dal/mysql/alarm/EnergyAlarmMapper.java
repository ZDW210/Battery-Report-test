package cn.iocoder.yudao.module.energy.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 移动储能告警 Mapper。
 */
@Mapper
public interface EnergyAlarmMapper extends BaseMapperX<EnergyAlarmDO> {

    default PageResult<EnergyAlarmDO> selectPage(EnergyAlarmPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyAlarmDO>()
                .eqIfPresent(EnergyAlarmDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyAlarmDO::getLevel, reqVO.getLevel())
                .eqIfPresent(EnergyAlarmDO::getStatus, reqVO.getStatus())
                .likeIfPresent(EnergyAlarmDO::getCode, reqVO.getCode())
                .likeIfPresent(EnergyAlarmDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(EnergyAlarmDO::getOccurTime, reqVO.getOccurTime())
                .orderByDesc(EnergyAlarmDO::getOccurTime)
                .orderByDesc(EnergyAlarmDO::getId));
    }

    default EnergyAlarmDO selectByAlarmNo(String alarmNo) {
        return selectOne(EnergyAlarmDO::getAlarmNo, alarmNo);
    }

}
