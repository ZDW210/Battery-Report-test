package cn.iocoder.yudao.module.energy.service.eiot;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.energy.controller.admin.eiot.vo.EnergyEiotSyncLogPageReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmRespVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.eiot.EnergyEiotSyncLogDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.dal.mysql.device.EnergyDeviceMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.eiot.EnergyEiotSyncLogMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.telemetry.EnergyTelemetryMapper;
import cn.iocoder.yudao.module.energy.service.alarm.EnergyAlarmService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * 移动储能 EIOT 接入 Service 实现类。
 */
@Service
@Validated
public class EnergyEiotServiceImpl implements EnergyEiotService {

    private static final String SYNC_TYPE_ALARM = "alarm";
    private static final String SYNC_TYPE_REALTIME = "realtime";
    private static final int SYNC_STATUS_SUCCESS = 0;
    private static final int SYNC_STATUS_FAILED = 1;
    private static final int DEVICE_STATUS_ONLINE = 0;
    private static final int DEVICE_STATUS_OFFLINE = 1;

    @Resource
    private EnergyAlarmService alarmService;
    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyEiotSyncLogMapper syncLogMapper;
    @Resource
    private EnergyTelemetryMapper telemetryMapper;
    @Resource
    private EnergyDeviceMapper deviceMapper;

    @Override
    public EnergyEiotAlarmRespVO receiveAlarm(EnergyEiotAlarmReqVO reqVO) {
        int created = 0;
        int ignored = 0;
        int failed = 0;
        List<EnergyEiotAlarmReqVO.AlarmItem> items = getAlarmItems(reqVO);
        for (EnergyEiotAlarmReqVO.AlarmItem item : items) {
            fillAlarmItemDefaults(reqVO, item);
            if (alarmService.getAlarmByAlarmNo(item.getAlarmNo()) != null) {
                ignored++;
                createSyncLog(reqVO, item, SYNC_STATUS_SUCCESS, "duplicate alarmNo ignored");
                continue;
            }

            EnergyDeviceDO device = getDevice(reqVO.getGatewaySn(), item.getMeterSn(), item.getMeterNo());
            if (device == null) {
                failed++;
                createSyncLog(reqVO, item, SYNC_STATUS_FAILED, "device not found by meterNo: " + item.getMeterNo());
                continue;
            }

            LocalDateTime occurTime = parseOccurTime(item);
            if (occurTime == null) {
                failed++;
                createSyncLog(reqVO, item, SYNC_STATUS_FAILED, "invalid occurTime: " + item.getOccurTime());
                continue;
            }

            alarmService.createAlarm(EnergyAlarmDO.builder()
                    .alarmNo(item.getAlarmNo())
                    .deviceId(device.getId())
                    .code(item.getCode())
                    .level(parseAlarmLevel(item.getLevel()))
                    .title(StrUtil.blankToDefault(item.getTitleText(), item.getCode()))
                    .content(item.getContentText())
                    .status(0)
                    .occurTime(occurTime)
                    .build());
            created++;
            createSyncLog(reqVO, item, SYNC_STATUS_SUCCESS, null);
        }
        return EnergyEiotAlarmRespVO.builder()
                .received(items.size())
                .created(created)
                .ignored(ignored)
                .failed(failed)
                .build();
    }

    @Override
    public EnergyEiotRealtimeRespVO receiveRealtime(EnergyEiotRealtimeReqVO reqVO) {
        String meterNo = buildMeterNo(reqVO.getGatewaySn(), reqVO.getMeterSn(), reqVO.getMeterNo());
        String requestId = buildRealtimeRequestId(reqVO, meterNo);
        EnergyDeviceDO device = getDevice(reqVO.getGatewaySn(), reqVO.getMeterSn(), meterNo);
        if (device == null) {
            createRealtimeSyncLog(reqVO, requestId, SYNC_STATUS_FAILED, "device not found by meterNo: " + meterNo);
            return EnergyEiotRealtimeRespVO.builder()
                    .received(1)
                    .updated(0)
                    .ignored(0)
                    .failed(1)
                    .build();
        }

        LocalDateTime collectTime = parseCollectTime(reqVO.getCreateTime(), reqVO.getTimestamp());
        if (collectTime == null) {
            createRealtimeSyncLog(reqVO, requestId, SYNC_STATUS_FAILED, "invalid createTime/timestamp");
            return EnergyEiotRealtimeRespVO.builder()
                    .received(1)
                    .updated(0)
                    .ignored(0)
                    .failed(1)
                    .build();
        }

        telemetryMapper.insert(EnergyTelemetryDO.builder()
                .deviceId(device.getId())
                .gatewaySn(reqVO.getGatewaySn())
                .meterSn(reqVO.getMeterSn())
                .meterNo(meterNo)
                .collectTime(collectTime)
                .timestamp(reqVO.getTimestamp())
                .source(reqVO.getSource())
                .state(reqVO.getState())
                .pa(reqVO.getPa())
                .pb(reqVO.getPb())
                .pc(reqVO.getPc())
                .ua(reqVO.getUa())
                .ub(reqVO.getUb())
                .uc(reqVO.getUc())
                .ia(reqVO.getIa())
                .ib(reqVO.getIb())
                .ic(reqVO.getIc())
                .p(reqVO.getP())
                .pf(reqVO.getPf())
                .epi(reqVO.getEpi())
                .build());

        deviceMapper.updateById(EnergyDeviceDO.builder()
                .id(device.getId())
                .status(parseDeviceStatus(reqVO.getState()))
                .lastPower(reqVO.getP())
                .lastVoltage(avg(reqVO.getUa(), reqVO.getUb(), reqVO.getUc()))
                .lastCurrent(avg(reqVO.getIa(), reqVO.getIb(), reqVO.getIc()))
                .lastReadingTime(collectTime)
                .build());
        createRealtimeSyncLog(reqVO, requestId, SYNC_STATUS_SUCCESS, null);
        return EnergyEiotRealtimeRespVO.builder()
                .received(1)
                .updated(1)
                .ignored(0)
                .failed(0)
                .build();
    }

    @Override
    public PageResult<EnergyEiotSyncLogDO> getSyncLogPage(EnergyEiotSyncLogPageReqVO pageReqVO) {
        return syncLogMapper.selectPage(pageReqVO);
    }

    private void createSyncLog(EnergyEiotAlarmReqVO reqVO, EnergyEiotAlarmReqVO.AlarmItem item, Integer status, String errorMsg) {
        syncLogMapper.insert(EnergyEiotSyncLogDO.builder()
                .syncType(SYNC_TYPE_ALARM)
                .requestId(StrUtil.blankToDefault(reqVO.getRequestId(), item.getAlarmNo()))
                .gatewaySn(reqVO.getGatewaySn())
                .meterSn(StrUtil.blankToDefault(item.getMeterSn(), item.getMeterNo()))
                .status(status)
                .errorMsg(errorMsg)
                .build());
    }

    private void createRealtimeSyncLog(EnergyEiotRealtimeReqVO reqVO, String requestId, Integer status, String errorMsg) {
        syncLogMapper.insert(EnergyEiotSyncLogDO.builder()
                .syncType(SYNC_TYPE_REALTIME)
                .requestId(requestId)
                .gatewaySn(reqVO.getGatewaySn())
                .meterSn(reqVO.getMeterSn())
                .status(status)
                .errorMsg(errorMsg)
                .build());
    }

    private LocalDateTime parseOccurTime(EnergyEiotAlarmReqVO.AlarmItem item) {
        try {
            return LocalDateTimeUtil.parse(item.getOccurTime(), DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private List<EnergyEiotAlarmReqVO.AlarmItem> getAlarmItems(EnergyEiotAlarmReqVO reqVO) {
        if (reqVO.getList() != null) {
            return reqVO.getList();
        }
        if (reqVO.getAlarms() != null) {
            return reqVO.getAlarms();
        }
        return Collections.emptyList();
    }

    private void fillAlarmItemDefaults(EnergyEiotAlarmReqVO reqVO, EnergyEiotAlarmReqVO.AlarmItem item) {
        if (StrUtil.isBlank(item.getMeterSn())) {
            item.setMeterSn(reqVO.getMeterSn());
        }
        if (StrUtil.isBlank(item.getMeterNo())) {
            item.setMeterNo(buildMeterNo(reqVO.getGatewaySn(), item.getMeterSn(), null));
        }
        if (StrUtil.isBlank(item.getOccurTime())) {
            item.setOccurTime(reqVO.getCreateTime());
        }
    }

    private String buildMeterNo(String gatewaySn, String meterSn, String meterNo) {
        if (StrUtil.isNotBlank(meterNo)) {
            return meterNo;
        }
        if (StrUtil.isAllNotBlank(gatewaySn, meterSn)) {
            return gatewaySn + "_" + meterSn;
        }
        return meterSn;
    }

    private EnergyDeviceDO getDevice(String gatewaySn, String meterSn, String meterNo) {
        if (StrUtil.isNotBlank(meterNo)) {
            EnergyDeviceDO device = deviceService.getDeviceByMeterNo(meterNo);
            if (device != null) {
                return device;
            }
        }
        if (StrUtil.isAllNotBlank(gatewaySn, meterSn)) {
            return deviceMapper.selectByGatewaySnAndMeterSn(gatewaySn, meterSn);
        }
        return null;
    }

    private String buildRealtimeRequestId(EnergyEiotRealtimeReqVO reqVO, String meterNo) {
        String timePart = reqVO.getTimestamp() != null ? reqVO.getTimestamp().toString() : reqVO.getCreateTime();
        return StrUtil.join("_", SYNC_TYPE_REALTIME, meterNo, timePart);
    }

    private LocalDateTime parseCollectTime(String createTime, Long timestamp) {
        if (StrUtil.isNotBlank(createTime)) {
            try {
                return LocalDateTimeUtil.parse(createTime, DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
            } catch (RuntimeException ignored) {
                // fallback to timestamp
            }
        }
        if (timestamp != null) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        }
        return null;
    }

    private Integer parseAlarmLevel(String value) {
        if (StrUtil.isBlank(value)) {
            return 1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private Integer parseDeviceStatus(String state) {
        if (StrUtil.equalsIgnoreCase(state, "OFFLINE")) {
            return DEVICE_STATUS_OFFLINE;
        }
        return DEVICE_STATUS_ONLINE;
    }

    private BigDecimal avg(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            if (value == null) {
                continue;
            }
            total = total.add(value);
            count++;
        }
        return count == 0 ? null : total.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP);
    }

}
