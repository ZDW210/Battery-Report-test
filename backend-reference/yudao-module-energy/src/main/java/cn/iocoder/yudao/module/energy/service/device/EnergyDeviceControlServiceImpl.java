package cn.iocoder.yudao.module.energy.service.device;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.device.vo.EnergyDeviceControlRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceControlLogDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.mysql.device.EnergyDeviceControlLogMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Validated
public class EnergyDeviceControlServiceImpl implements EnergyDeviceControlService {

    private static final int STATUS_SUCCESS = 0;
    private static final int STATUS_FAILED = 1;
    private static final String METHOD_SWITCH = "SWITCH";
    private static final String METHOD_REFRESH = "REFRESH";

    @Value("${energy.eiot.control-url:}")
    private String controlUrl;
    @Value("${energy.eiot.control-token:}")
    private String controlToken;

    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyDeviceControlLogMapper controlLogMapper;

    @Override
    public EnergyDeviceControlRespVO controlDevice(EnergyDeviceControlReqVO reqVO, Long operatorUserId) {
        EnergyDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        String method = reqVO.getMethod().toUpperCase(Locale.ROOT);
        Map<String, Object> value;
        try {
            value = normalizeValue(method, reqVO.getValue());
        } catch (IllegalArgumentException exception) {
            return EnergyDeviceControlRespVO.builder()
                    .success(false)
                    .message(exception.getMessage())
                    .build();
        }
        Map<String, Object> requestPayload = buildRequestPayload(device, method, value);

        EnergyDeviceControlLogDO log = EnergyDeviceControlLogDO.builder()
                .deviceId(device.getId())
                .deviceNo(device.getDeviceNo())
                .gatewaySn(device.getGatewaySn())
                .meterSn(device.getMeterSn())
                .meterNo(device.getMeterNo())
                .method(method)
                .value(JsonUtils.toJsonString(value))
                .requestPayload(JsonUtils.toJsonString(requestPayload))
                .status(STATUS_FAILED)
                .operatorUserId(operatorUserId)
                .operateTime(LocalDateTime.now())
                .build();
        controlLogMapper.insert(log);

        if (StrUtil.isBlank(controlUrl)) {
            return updateFailed(log.getId(), "EIOT 控制地址未配置，请配置 energy.eiot.control-url");
        }

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=UTF-8");
            if (StrUtil.isNotBlank(controlToken)) {
                headers.put("Authorization", "Bearer " + controlToken);
            }
            String responseBody = HttpUtils.post(controlUrl, headers, JsonUtils.toJsonString(requestPayload));
            Map<String, Object> response = JsonUtils.parseObjectQuietly(responseBody, new TypeReference<Map<String, Object>>() {});
            boolean success = isEiotSuccess(response);
            String code = response == null ? null : objectToString(response.get("code"));
            String message = getEiotMessage(response, success ? "EIOT 控制指令已下发" : "EIOT 控制指令下发失败");
            updateResult(log.getId(), success ? STATUS_SUCCESS : STATUS_FAILED, responseBody, code, success ? null : message);
            return EnergyDeviceControlRespVO.builder()
                    .controlLogId(log.getId())
                    .success(success)
                    .message(message)
                    .build();
        } catch (RuntimeException exception) {
            return updateFailed(log.getId(), "EIOT 控制请求异常：" + exception.getMessage());
        }
    }

    private Map<String, Object> normalizeValue(String method, Map<String, Object> value) {
        if (!METHOD_SWITCH.equals(method) && !METHOD_REFRESH.equals(method)) {
            throw new IllegalArgumentException("暂不支持的控制方法：" + method);
        }
        Map<String, Object> normalized = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        if (METHOD_REFRESH.equals(method)) {
            return normalized;
        }
        Object switchValue = normalized.get("Switch");
        if (switchValue == null) {
            switchValue = normalized.get("switch");
        }
        String switchText = objectToString(switchValue);
        if (!"0".equals(switchText) && !"1".equals(switchText)) {
            throw new IllegalArgumentException("SWITCH 控制值只能为 0 或 1");
        }
        normalized.clear();
        normalized.put("Switch", switchText);
        return normalized;
    }

    private Map<String, Object> buildRequestPayload(EnergyDeviceDO device, String method, Map<String, Object> value) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gatewaySn", device.getGatewaySn());
        payload.put("meterSn", device.getMeterSn());
        payload.put("meterNo", device.getMeterNo());
        payload.put("method", method);
        payload.put("value", value);
        return payload;
    }

    private boolean isEiotSuccess(Map<String, Object> response) {
        if (response == null) {
            return false;
        }
        Object success = response.get("success");
        if (success instanceof Boolean) {
            return (Boolean) success;
        }
        String code = objectToString(response.get("code"));
        String status = objectToString(response.get("status"));
        return "0".equals(code) || "200".equals(code) || "SUCCESS".equalsIgnoreCase(code)
                || "0".equals(status) || "SUCCESS".equalsIgnoreCase(status);
    }

    private String getEiotMessage(Map<String, Object> response, String defaultMessage) {
        if (response == null) {
            return defaultMessage;
        }
        Object message = response.get("message");
        if (message == null) {
            message = response.get("msg");
        }
        return message == null ? defaultMessage : objectToString(message);
    }

    private String objectToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private EnergyDeviceControlRespVO updateFailed(Long logId, String message) {
        updateResult(logId, STATUS_FAILED, null, null, message);
        return EnergyDeviceControlRespVO.builder()
                .controlLogId(logId)
                .success(false)
                .message(message)
                .build();
    }

    private void updateResult(Long logId, Integer status, String responsePayload, String eiotCode, String errorMsg) {
        controlLogMapper.updateById(EnergyDeviceControlLogDO.builder()
                .id(logId)
                .status(status)
                .responsePayload(responsePayload)
                .eiotCode(eiotCode)
                .errorMsg(errorMsg)
                .build());
    }

}
