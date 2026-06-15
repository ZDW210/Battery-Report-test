package cn.iocoder.yudao.module.energy.service.app;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionStartReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceRespVO;
import cn.iocoder.yudao.module.energy.controller.app.home.vo.AppEnergyHomeOverviewRespVO;
import cn.iocoder.yudao.module.energy.controller.app.session.vo.AppEnergyScanDeviceRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.alarm.EnergyAlarmDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAccountEventDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAppUserDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.permission.EnergyUserScopeDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.vehicle.EnergyVehicleDO;
import cn.iocoder.yudao.module.energy.dal.mysql.alarm.EnergyAlarmMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAccountEventMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAppUserMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.device.EnergyDeviceMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.permission.EnergyUserScopeMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.session.EnergyChargeSessionMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.telemetry.EnergyTelemetryMapper;
import cn.iocoder.yudao.module.energy.dal.mysql.vehicle.EnergyVehicleMapper;
import cn.iocoder.yudao.module.energy.service.session.EnergyChargeSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class EnergyAppServiceImpl implements EnergyAppService {

    private static final int DEVICE_STATUS_ONLINE = 0;
    private static final int DEVICE_STATUS_FAULT = 2;
    private static final int ALARM_STATUS_UNACKED = 0;

    @Resource
    private EnergyDeviceMapper deviceMapper;
    @Resource
    private EnergyAlarmMapper alarmMapper;
    @Resource
    private EnergyUserScopeMapper userScopeMapper;
    @Resource
    private EnergyChargeSessionService chargeSessionService;
    @Resource
    private EnergyChargeSessionMapper chargeSessionMapper;
    @Resource
    private EnergyVehicleMapper vehicleMapper;
    @Resource
    private EnergyAppUserMapper appUserMapper;
    @Resource
    private EnergyAccountEventMapper accountEventMapper;
    @Resource
    private EnergyTelemetryMapper telemetryMapper;

    @Override
    public AppEnergyHomeOverviewRespVO getHomeOverview() {
        List<EnergyDeviceDO> devices = getAuthorizedDevices(new AppEnergyDeviceListReqVO());
        List<Long> deviceIds = devices.stream().map(EnergyDeviceDO::getId).toList();
        return AppEnergyHomeOverviewRespVO.builder()
                .deviceCount((long) devices.size())
                .onlineCount(countDeviceByStatus(devices, DEVICE_STATUS_ONLINE))
                .faultCount(countDeviceByStatus(devices, DEVICE_STATUS_FAULT))
                .unackedAlarmCount(countUnackedAlarms(deviceIds))
                .todayChargeEnergy(BigDecimal.ZERO)
                .todayDischargeEnergy(BigDecimal.ZERO)
                .currentPower(sumCurrentPower(devices))
                .build();
    }

    @Override
    public List<AppEnergyDeviceRespVO> getDeviceList(AppEnergyDeviceListReqVO reqVO) {
        return getAuthorizedDevices(reqVO).stream()
                .map(this::buildAppDeviceRespVO)
                .toList();
    }

    @Override
    public EnergyDeviceDO getAuthorizedDevice(Long deviceId) {
        EnergyDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        if (!isDeviceAuthorized(device, getAuthorizedScope())) {
            throw exception(APP_SCAN_DEVICE_FORBIDDEN);
        }
        return device;
    }

    private AppEnergyDeviceRespVO buildAppDeviceRespVO(EnergyDeviceDO device) {
        AppEnergyDeviceRespVO respVO = BeanUtils.toBean(device, AppEnergyDeviceRespVO.class);
        EnergyTelemetryDO latest = telemetryMapper.selectOne(new LambdaQueryWrapperX<EnergyTelemetryDO>()
                .eq(EnergyTelemetryDO::getDeviceId, device.getId())
                .orderByDesc(EnergyTelemetryDO::getCollectTime)
                .orderByDesc(EnergyTelemetryDO::getId)
                .last("LIMIT 1"));
        if (latest != null) {
            respVO.setLastPower(latest.getP());
            respVO.setLastVoltage(avg(latest.getUa(), latest.getUb(), latest.getUc()));
            respVO.setLastCurrent(avg(latest.getIa(), latest.getIb(), latest.getIc()));
            respVO.setLastEpi(latest.getEpi());
            respVO.setLastReadingTime(latest.getCollectTime());
        }
        return respVO;
    }

    private BigDecimal avg(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            if (value != null) {
                total = total.add(value);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 3, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public EnergyTelemetryDO getLatestTelemetry(Long deviceId) {
        getAuthorizedDevice(deviceId);
        return telemetryMapper.selectOne(new LambdaQueryWrapperX<EnergyTelemetryDO>()
                .eq(EnergyTelemetryDO::getDeviceId, deviceId)
                .orderByDesc(EnergyTelemetryDO::getCollectTime)
                .orderByDesc(EnergyTelemetryDO::getId)
                .last("LIMIT 1"));
    }

    @Override
    public List<EnergyTelemetryDO> getTelemetryList(Long deviceId, LocalDateTime from, LocalDateTime to, Integer limit) {
        getAuthorizedDevice(deviceId);
        LambdaQueryWrapperX<EnergyTelemetryDO> query = new LambdaQueryWrapperX<>();
        query.eq(EnergyTelemetryDO::getDeviceId, deviceId);
        if (from != null && to != null) {
            query.between(EnergyTelemetryDO::getCollectTime, from, to);
        } else if (from != null) {
            query.ge(EnergyTelemetryDO::getCollectTime, from);
        } else if (to != null) {
            query.le(EnergyTelemetryDO::getCollectTime, to);
        }
        query.orderByAsc(EnergyTelemetryDO::getCollectTime)
                .orderByAsc(EnergyTelemetryDO::getId);
        query.last("LIMIT " + Math.min(Math.max(limit == null ? 500 : limit, 1), 2000));
        return telemetryMapper.selectList(query);
    }

    @Override
    public List<EnergyAlarmDO> getAlarmList(Long deviceId, Integer limit) {
        if (deviceId != null) {
            getAuthorizedDevice(deviceId);
        }
        List<Long> authorizedDeviceIds = getAuthorizedDevices(new AppEnergyDeviceListReqVO()).stream()
                .map(EnergyDeviceDO::getId)
                .toList();
        if (authorizedDeviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<EnergyAlarmDO> query = new LambdaQueryWrapperX<>();
        query.in(EnergyAlarmDO::getDeviceId, authorizedDeviceIds);
        query.eqIfPresent(EnergyAlarmDO::getDeviceId, deviceId);
        query.orderByDesc(EnergyAlarmDO::getOccurTime)
                .orderByDesc(EnergyAlarmDO::getId)
                .last("LIMIT " + Math.min(Math.max(limit == null ? 100 : limit, 1), 500));
        return alarmMapper.selectList(query);
    }

    @Override
    public EnergyAlarmDO getAlarm(Long alarmId) {
        EnergyAlarmDO alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw exception(ALARM_NOT_EXISTS);
        }
        getAuthorizedDevice(alarm.getDeviceId());
        return alarm;
    }

    @Override
    public List<EnergyChargeSessionDO> getChargeSessionList(Long deviceId, Long customerId,
                                                            Collection<Integer> statuses,
                                                            LocalDateTime from, LocalDateTime to, Integer limit) {
        List<Long> authorizedDeviceIds = getAuthorizedDevices(new AppEnergyDeviceListReqVO()).stream()
                .map(EnergyDeviceDO::getId)
                .toList();
        if (authorizedDeviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<EnergyChargeSessionDO> query = new LambdaQueryWrapperX<>();
        query.in(EnergyChargeSessionDO::getDeviceId, authorizedDeviceIds)
                .eqIfPresent(EnergyChargeSessionDO::getDeviceId, deviceId)
                .eqIfPresent(EnergyChargeSessionDO::getCustomerId, customerId);
        if (statuses != null && !statuses.isEmpty()) {
            query.in(EnergyChargeSessionDO::getStatus, statuses);
        }
        if (from != null && to != null) {
            query.between(EnergyChargeSessionDO::getStartTime, from, to);
        } else if (from != null) {
            query.ge(EnergyChargeSessionDO::getStartTime, from);
        } else if (to != null) {
            query.le(EnergyChargeSessionDO::getStartTime, to);
        }
        query.orderByDesc(EnergyChargeSessionDO::getStartTime)
                .orderByDesc(EnergyChargeSessionDO::getId)
                .last("LIMIT " + Math.min(Math.max(limit == null ? 100 : limit, 1), 500));
        return chargeSessionMapper.selectList(query);
    }

    @Override
    public AppEnergyScanDeviceRespVO verifyScanDevice(String scanText) {
        return verifyScanDevice(scanText, null, null);
    }

    @Override
    public AppEnergyScanDeviceRespVO verifyScanDevice(String scanText, String authType, String cardNo) {
        ScannedDevice scanned = getScannedDevice(scanText);
        AccountCheck accountCheck = resolveAccount(authType, cardNo, scanned.device());
        AppEnergyScanDeviceRespVO respVO = buildScanDeviceResp(scanned, accountCheck);
        recordAccountEvent("VERIFY", scanText, scanned.device(), accountCheck);
        return respVO;
    }

    @Override
    public EnergyChargeSessionDO startDischargeByScan(String scanText) {
        return startDischargeByScan(scanText, null, null);
    }

    @Override
    public EnergyChargeSessionDO startDischargeByScan(String scanText, String authType, String cardNo) {
        ScannedDevice scanned = getScannedDevice(scanText);
        EnergyDeviceDO device = scanned.device();
        AccountCheck accountCheck = resolveAccount(authType, cardNo, device);
        if (!accountCheck.known()) {
            recordAccountEvent("DISCHARGE", scanText, device, accountCheck);
            throw exception(APP_SCAN_ACCOUNT_UNKNOWN);
        }
        EnergyChargeSessionStartReqVO reqVO = new EnergyChargeSessionStartReqVO();
        reqVO.setDeviceId(device.getId());
        reqVO.setSessionType(1);
        try {
            EnergyChargeSessionDO session = chargeSessionService.startChargeSession(reqVO, accountCheck.user().getId());
            recordAccountEvent("DISCHARGE", scanText, device, accountCheck, "账户已识别，放电任务已开始");
            return session;
        } catch (ServiceException ex) {
            recordAccountEvent("DISCHARGE", scanText, device, accountCheck,
                    "账户已识别，放电启动失败：" + safeMessage(ex));
            throw ex;
        } catch (RuntimeException ex) {
            recordAccountEvent("DISCHARGE", scanText, device, accountCheck,
                    "账户已识别，放电启动失败");
            throw ex;
        }
    }

    private List<EnergyDeviceDO> getAuthorizedDevices(AppEnergyDeviceListReqVO reqVO) {
        AuthorizedScope scope = getAuthorizedScope();
        if (scope.isEmpty()) {
            return Collections.emptyList();
        }
        return deviceMapper.selectAppList(reqVO, scope.customerIds(), scope.projectIds(), scope.deviceIds());
    }

    private ScannedDevice getScannedDevice(String scanText) {
        Map<String, String> params = parseScanParams(scanText);
        EnergyDeviceDO device = getScanDevice(params);
        if (device != null) {
            return new ScannedDevice(null, device);
        }
        EnergyVehicleDO vehicle = getScanVehicle(params);
        if (vehicle == null || vehicle.getDeviceId() == null) {
            throw exception(APP_SCAN_DEVICE_NOT_EXISTS);
        }
        device = deviceMapper.selectById(vehicle.getDeviceId());
        if (device == null) {
            throw exception(APP_SCAN_DEVICE_NOT_EXISTS);
        }
        return new ScannedDevice(vehicle, device);
    }

    private EnergyDeviceDO getScanDevice(Map<String, String> params) {
        EnergyDeviceDO device = null;
        String deviceId = firstNotBlank(params, "deviceId", "device_id", "id");
        if (deviceId != null) {
            try {
                device = deviceMapper.selectById(Long.valueOf(deviceId));
            } catch (NumberFormatException ignored) {
                device = null;
            }
        }
        if (device == null) {
            String deviceNo = firstNotBlank(params, "deviceNo", "device_no");
            if (deviceNo != null) {
                device = deviceMapper.selectByDeviceNo(deviceNo);
            }
        }
        if (device == null) {
            String meterNo = firstNotBlank(params, "meterNo", "meter_no");
            if (meterNo != null) {
                device = deviceMapper.selectByMeterNo(meterNo);
            }
        }
        if (device == null) {
            String gatewaySn = firstNotBlank(params, "gatewaySn", "gateway_sn");
            String meterSn = firstNotBlank(params, "meterSn", "meter_sn");
            if (gatewaySn != null && meterSn != null) {
                device = deviceMapper.selectByGatewaySnAndMeterSn(gatewaySn, meterSn);
            }
        }
        if (device == null) {
            String rawCode = params.get("rawCode");
            if (rawCode != null) {
                device = deviceMapper.selectByDeviceNo(rawCode);
            }
        }
        return device;
    }

    private EnergyVehicleDO getScanVehicle(Map<String, String> params) {
        String vehicleId = firstNotBlank(params, "vehicleId", "vehicle_id");
        if (vehicleId != null) {
            try {
                EnergyVehicleDO vehicle = vehicleMapper.selectById(Long.valueOf(vehicleId));
                if (vehicle != null) {
                    return vehicle;
                }
            } catch (NumberFormatException ignored) {
                // Continue with other vehicle identifiers.
            }
        }
        String qrCode = firstNotBlank(params, "qrCode", "qr_code", "code");
        if (qrCode != null) {
            EnergyVehicleDO vehicle = vehicleMapper.selectByQrCode(qrCode);
            if (vehicle != null) {
                return vehicle;
            }
        }
        String vehicleNo = firstNotBlank(params, "vehicleNo", "vehicle_no");
        if (vehicleNo != null) {
            EnergyVehicleDO vehicle = vehicleMapper.selectByVehicleNo(vehicleNo);
            if (vehicle != null) {
                return vehicle;
            }
        }
        String plateNo = firstNotBlank(params, "plateNo", "plate_no", "carNo", "car_no");
        if (plateNo != null) {
            EnergyVehicleDO vehicle = vehicleMapper.selectByPlateNo(plateNo);
            if (vehicle != null) {
                return vehicle;
            }
        }
        String rawCode = params.get("rawCode");
        return rawCode == null ? null : vehicleMapper.selectFirstByCode(rawCode);
    }

    private Map<String, String> parseScanParams(String scanText) {
        if (scanText == null || scanText.trim().isEmpty()) {
            throw exception(APP_SCAN_CODE_EMPTY);
        }
        String text = scanText.trim();
        String queryText = text;
        int questionIndex = text.indexOf('?');
        if (questionIndex >= 0 && questionIndex < text.length() - 1) {
            queryText = text.substring(questionIndex + 1);
        }
        Map<String, String> params = new LinkedHashMap<>();
        if (queryText.contains("=")) {
            for (String part : queryText.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && !pair[0].isBlank()) {
                    params.put(decode(pair[0]), decode(pair[1]));
                }
            }
        }
        if (!params.isEmpty()) {
            return params;
        }
        params.put("rawCode", text);
        params.put(text.matches("\\d+") ? "deviceId" : "deviceNo", text);
        params.put("meterNo", text);
        return params;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String firstNotBlank(Map<String, String> params, String... keys) {
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isDeviceAuthorized(EnergyDeviceDO device, AuthorizedScope scope) {
        if (scope.isEmpty()) {
            return false;
        }
        return scope.deviceIds().contains(device.getId())
                || device.getProjectId() != null && scope.projectIds().contains(device.getProjectId())
                || device.getCustomerId() != null && scope.customerIds().contains(device.getCustomerId());
    }

    private AccountCheck resolveAccount(String authType, String cardNo, EnergyDeviceDO device) {
        String type = authType == null || authType.isBlank() ? "WECHAT" : authType.trim().toUpperCase();
        EnergyAppUserDO user;
        if ("CARD".equals(type)) {
            if (cardNo == null || cardNo.isBlank()) {
                return new AccountCheck(false, type, null, cardNo, "未知账户");
            }
            user = appUserMapper.selectByCardNo(cardNo.trim());
        } else {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            user = loginUser == null ? null : appUserMapper.selectById(loginUser.getId());
        }
        if (user == null || !Objects.equals(user.getStatus(), 0)) {
            return new AccountCheck(false, type, user, cardNo, "未知账户");
        }
        if (!isDeviceAuthorized(device, getAuthorizedScope(user.getId(), 1))) {
            return new AccountCheck(false, type, user, cardNo, "未知账户");
        }
        return new AccountCheck(true, type, user, cardNo, "账户已识别");
    }

    private AppEnergyScanDeviceRespVO buildScanDeviceResp(ScannedDevice scanned, AccountCheck accountCheck) {
        EnergyVehicleDO vehicle = scanned.vehicle();
        EnergyDeviceDO device = scanned.device();
        EnergyAppUserDO user = accountCheck.user();
        return AppEnergyScanDeviceRespVO.builder()
                .accountKnown(accountCheck.known())
                .accountType(accountCheck.type())
                .accountId(user == null ? null : user.getId())
                .accountName(user == null ? null : user.getNickname())
                .accountMobile(user == null ? null : user.getMobile())
                .cardNo(user == null ? accountCheck.cardNo() : user.getCardNo())
                .message(accountCheck.message())
                .vehicleId(vehicle == null ? null : vehicle.getId())
                .vehicleNo(vehicle == null ? null : vehicle.getVehicleNo())
                .plateNo(vehicle == null ? null : vehicle.getPlateNo())
                .qrCode(vehicle == null ? null : vehicle.getQrCode())
                .deviceId(device.getId())
                .deviceNo(device.getDeviceNo())
                .deviceName(device.getDeviceName())
                .meterNo(device.getMeterNo())
                .gatewaySn(device.getGatewaySn())
                .meterSn(device.getMeterSn())
                .customerId(device.getCustomerId())
                .projectId(device.getProjectId())
                .status(device.getStatus())
                .lastPower(device.getLastPower())
                .lastReadingTime(device.getLastReadingTime())
                .build();
    }

    private void recordAccountEvent(String eventScene, String scanText, EnergyDeviceDO device, AccountCheck accountCheck) {
        recordAccountEvent(eventScene, scanText, device, accountCheck, accountCheck.message());
    }

    private void recordAccountEvent(String eventScene, String scanText, EnergyDeviceDO device, AccountCheck accountCheck,
                                    String resultMessage) {
        EnergyAppUserDO user = accountCheck.user();
        accountEventMapper.insert(EnergyAccountEventDO.builder()
                .eventScene(eventScene)
                .authType(accountCheck.type())
                .scanText(scanText)
                .cardNo(user == null ? accountCheck.cardNo() : user.getCardNo())
                .accountKnown(accountCheck.known())
                .accountId(user == null ? null : user.getId())
                .accountName(user == null ? null : user.getNickname())
                .accountMobile(user == null ? null : user.getMobile())
                .deviceId(device.getId())
                .deviceNo(device.getDeviceNo())
                .deviceName(device.getDeviceName())
                .meterNo(device.getMeterNo())
                .gatewaySn(device.getGatewaySn())
                .meterSn(device.getMeterSn())
                .customerId(device.getCustomerId())
                .projectId(device.getProjectId())
                .resultMessage(resultMessage)
                .build());
    }

    private String safeMessage(ServiceException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "业务校验未通过" : ex.getMessage();
    }

    private record ScannedDevice(EnergyVehicleDO vehicle, EnergyDeviceDO device) {
    }

    private record AccountCheck(boolean known, String type, EnergyAppUserDO user, String cardNo, String message) {
    }

    private AuthorizedScope getAuthorizedScope() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return AuthorizedScope.empty();
        }
        return getAuthorizedScope(loginUser.getId(), loginUser.getUserType());
    }

    private AuthorizedScope getAuthorizedScope(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            return AuthorizedScope.empty();
        }
        List<EnergyUserScopeDO> scopes = userScopeMapper.selectEnabledListByUser(userId, userType);
        return new AuthorizedScope(
                collectIds(scopes, ScopeField.CUSTOMER),
                collectIds(scopes, ScopeField.PROJECT),
                collectIds(scopes, ScopeField.DEVICE));
    }

    private Set<Long> collectIds(List<EnergyUserScopeDO> scopes, ScopeField field) {
        return scopes.stream()
                .map(scope -> switch (field) {
                    case CUSTOMER -> scope.getCustomerId();
                    case PROJECT -> scope.getProjectId();
                    case DEVICE -> scope.getDeviceId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Long countDeviceByStatus(List<EnergyDeviceDO> devices, Integer status) {
        return devices.stream()
                .filter(device -> Objects.equals(device.getStatus(), status))
                .count();
    }

    private BigDecimal sumCurrentPower(List<EnergyDeviceDO> devices) {
        return devices.stream()
                .map(EnergyDeviceDO::getLastPower)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long countUnackedAlarms(List<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return 0L;
        }
        return alarmMapper.selectCount(new LambdaQueryWrapperX<EnergyAlarmDO>()
                .eq(EnergyAlarmDO::getStatus, ALARM_STATUS_UNACKED)
                .in(EnergyAlarmDO::getDeviceId, deviceIds));
    }

    private enum ScopeField {
        CUSTOMER,
        PROJECT,
        DEVICE
    }

    private record AuthorizedScope(Set<Long> customerIds, Set<Long> projectIds, Set<Long> deviceIds) {

        static AuthorizedScope empty() {
            return new AuthorizedScope(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        }

        boolean isEmpty() {
            return customerIds.isEmpty() && projectIds.isEmpty() && deviceIds.isEmpty();
        }

    }

}
