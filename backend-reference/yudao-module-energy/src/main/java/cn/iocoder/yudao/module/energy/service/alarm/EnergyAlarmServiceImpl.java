package cn.iocoder.yudao.module.energy.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.alarm.vo.EnergyAlarmPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmHandleRecordDO;
import cn.iocoder.yudao.module.energy.dal.mysql.alarm.EnergyAlarmMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.alarm.EnergyAlarmHandleRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

/**
 * 移动储能告警 Service 实现类。
 */
@Service
@Validated
public class EnergyAlarmServiceImpl implements EnergyAlarmService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_CLOSED = 2;

    @Resource
    private EnergyAlarmMapper alarmMapper;
    @Resource
    private EnergyAlarmHandleRecordMapper alarmHandleRecordMapper;

    @Override
    public PageResult<EnergyAlarmDO> getAlarmPage(EnergyAlarmPageReqVO pageReqVO) {
        return alarmMapper.selectPage(pageReqVO);
    }

    @Override
    public EnergyAlarmDO getAlarm(Long id) {
        return alarmMapper.selectById(id);
    }

    @Override
    public void ackAlarm(Long id, Long userId, String remark) {
        EnergyAlarmDO alarm = validateAlarmExists(id);
        if (alarm.getStatus() != STATUS_PENDING) {
            throw exception(ALARM_STATUS_NOT_PENDING);
        }
        LocalDateTime now = LocalDateTime.now();
        alarmMapper.updateById(EnergyAlarmDO.builder()
                .id(id)
                .status(1)
                .ackUserId(userId)
                .ackTime(now)
                .build());
        createHandleRecord(id, "ack", userId, remark, now);
    }

    @Override
    public void closeAlarm(Long id, Long userId, String remark) {
        EnergyAlarmDO alarm = validateAlarmExists(id);
        if (alarm.getStatus() == STATUS_CLOSED) {
            throw exception(ALARM_STATUS_CLOSED);
        }
        LocalDateTime now = LocalDateTime.now();
        alarmMapper.updateById(EnergyAlarmDO.builder()
                .id(id)
                .status(STATUS_CLOSED)
                .closeTime(now)
                .build());
        createHandleRecord(id, "close", userId, remark, now);
    }

    @Override
    public EnergyAlarmDO getAlarmByAlarmNo(String alarmNo) {
        return alarmMapper.selectByAlarmNo(alarmNo);
    }

    @Override
    public Long createAlarm(EnergyAlarmDO alarm) {
        alarmMapper.insert(alarm);
        return alarm.getId();
    }

    @Override
    public EnergyAlarmDO validateAlarmExists(Long id) {
        EnergyAlarmDO alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(ALARM_NOT_EXISTS);
        }
        return alarm;
    }

    private void createHandleRecord(Long alarmId, String action, Long userId, String remark, LocalDateTime operateTime) {
        alarmHandleRecordMapper.insert(EnergyAlarmHandleRecordDO.builder()
                .alarmId(alarmId)
                .action(action)
                .remark(remark)
                .operatorUserId(userId)
                .operateTime(operateTime)
                .build());
    }

}
