package cn.iocoder.yudao.module.energy.dal.mysql.alarm;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmHandleRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 移动储能告警处理记录 Mapper。
 */
@Mapper
public interface EnergyAlarmHandleRecordMapper extends BaseMapperX<EnergyAlarmHandleRecordDO> {
}
