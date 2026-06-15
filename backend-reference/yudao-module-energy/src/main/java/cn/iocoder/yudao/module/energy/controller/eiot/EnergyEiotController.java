package cn.iocoder.yudao.module.energy.controller.eiot;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionRespVO;
import cn.iocoder.yudao.module.energy.controller.app.session.vo.AppEnergyScanDeviceRespVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotAlarmRespVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotCardReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeReqVO;
import cn.iocoder.yudao.module.energy.controller.eiot.vo.EnergyEiotRealtimeRespVO;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import cn.iocoder.yudao.module.energy.service.eiot.EnergyEiotService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "EIOT - 移动储能接入")
@RestController
@RequestMapping("/infra-api/energy/eiot")
@Validated
public class EnergyEiotController {

    private static final Long DEFAULT_EIOT_TENANT_ID = 1L;
    private static final String EIOT_TOKEN_HEADER = "X-EIOT-Token";

    @Resource
    private EnergyEiotService eiotService;
    @Resource
    private EnergyAppService appService;

    @Value("${energy.eiot.inbound-token:}")
    private String inboundToken;

    @PostMapping("/alarm")
    @Operation(summary = "接入 EIOT 告警")
    @PermitAll
    public CommonResult<EnergyEiotAlarmRespVO> receiveAlarm(
            @RequestHeader(value = EIOT_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody EnergyEiotAlarmReqVO reqVO) {
        validateInboundToken(token);
        return success(withDefaultTenant(() -> eiotService.receiveAlarm(reqVO)));
    }

    @PostMapping("/realtime")
    @Operation(summary = "接入 EIOT 实时数据")
    @PermitAll
    public CommonResult<EnergyEiotRealtimeRespVO> receiveRealtime(
            @RequestHeader(value = EIOT_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody EnergyEiotRealtimeReqVO reqVO) {
        validateInboundToken(token);
        return success(withDefaultTenant(() -> eiotService.receiveRealtime(reqVO)));
    }

    @PostMapping("/card/verify")
    @Operation(summary = "刷卡校验账户和设备")
    @PermitAll
    public CommonResult<AppEnergyScanDeviceRespVO> verifyCard(
            @RequestHeader(value = EIOT_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody EnergyEiotCardReqVO reqVO) {
        validateInboundToken(token);
        return success(withDefaultTenant(() -> appService.verifyScanDevice(reqVO.getScanText(), "CARD", reqVO.getCardNo())));
    }

    @PostMapping("/card/discharge")
    @Operation(summary = "刷卡启动放电")
    @PermitAll
    public CommonResult<EnergyChargeSessionRespVO> startDischargeByCard(
            @RequestHeader(value = EIOT_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody EnergyEiotCardReqVO reqVO) {
        validateInboundToken(token);
        return success(withDefaultTenant(() -> BeanUtils.toBean(
                appService.startDischargeByScan(reqVO.getScanText(), "CARD", reqVO.getCardNo()),
                EnergyChargeSessionRespVO.class)));
    }

    private void validateInboundToken(String token) {
        if (StrUtil.isBlank(inboundToken)) {
            throw exception0(FORBIDDEN.getCode(), "EIOT inbound token is not configured");
        }
        if (!StrUtil.equals(inboundToken, token)) {
            throw exception0(FORBIDDEN.getCode(), "EIOT inbound token invalid");
        }
    }

    private <T> T withDefaultTenant(Supplier<T> supplier) {
        if (TenantContextHolder.getTenantId() != null) {
            return supplier.get();
        }
        TenantContextHolder.setTenantId(DEFAULT_EIOT_TENANT_ID);
        return supplier.get();
    }

}
