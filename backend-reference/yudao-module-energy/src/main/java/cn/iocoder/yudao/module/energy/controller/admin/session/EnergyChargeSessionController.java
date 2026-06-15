package cn.iocoder.yudao.module.energy.controller.admin.session;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.*;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.session.EnergyChargeSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 移动储能充放电会话")
@RestController
@RequestMapping("/energy/charge-session")
@Validated
public class EnergyChargeSessionController {

    @Resource
    private EnergyChargeSessionService chargeSessionService;
    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyCustomerService customerService;

    @GetMapping("/get")
    @Operation(summary = "获得充放电会话")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:charge-session:query')")
    public CommonResult<EnergyChargeSessionRespVO> getChargeSession(@RequestParam("id") Long id) {
        EnergyChargeSessionRespVO respVO = BeanUtils.toBean(chargeSessionService.getChargeSession(id),
                EnergyChargeSessionRespVO.class);
        fillNames(List.of(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得充放电会话分页")
    @PreAuthorize("@ss.hasPermission('energy:charge-session:query')")
    public CommonResult<PageResult<EnergyChargeSessionRespVO>> getChargeSessionPage(
            @Valid EnergyChargeSessionPageReqVO pageReqVO) {
        PageResult<EnergyChargeSessionDO> pageResult = chargeSessionService.getChargeSessionPage(pageReqVO);
        PageResult<EnergyChargeSessionRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyChargeSessionRespVO.class);
        fillNames(respPageResult.getList());
        return success(respPageResult);
    }

    @PostMapping("/start")
    @Operation(summary = "开始充放电会话")
    @PreAuthorize("@ss.hasPermission('energy:charge-session:start')")
    public CommonResult<EnergyChargeSessionRespVO> startChargeSession(
            @Valid @RequestBody EnergyChargeSessionStartReqVO reqVO) {
        EnergyChargeSessionRespVO respVO = BeanUtils.toBean(chargeSessionService.startChargeSession(reqVO, getLoginUserId()),
                EnergyChargeSessionRespVO.class);
        fillNames(List.of(respVO));
        return success(respVO);
    }

    @PostMapping("/stop")
    @Operation(summary = "结束充放电会话")
    @PreAuthorize("@ss.hasPermission('energy:charge-session:stop')")
    public CommonResult<EnergyChargeSessionRespVO> stopChargeSession(
            @Valid @RequestBody EnergyChargeSessionStopReqVO reqVO) {
        EnergyChargeSessionRespVO respVO = BeanUtils.toBean(chargeSessionService.stopChargeSession(reqVO, getLoginUserId()),
                EnergyChargeSessionRespVO.class);
        fillNames(List.of(respVO));
        return success(respVO);
    }

    @PostMapping("/settle")
    @Operation(summary = "结算充放电会话")
    @PreAuthorize("@ss.hasPermission('energy:charge-session:settle')")
    public CommonResult<EnergyChargeSessionRespVO> settleChargeSession(
            @Valid @RequestBody EnergyChargeSessionSettleReqVO reqVO) {
        EnergyChargeSessionRespVO respVO = BeanUtils.toBean(chargeSessionService.settleChargeSession(reqVO.getSessionId()),
                EnergyChargeSessionRespVO.class);
        fillNames(List.of(respVO));
        return success(respVO);
    }

    private void fillNames(List<EnergyChargeSessionRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyDeviceDO> deviceMap = deviceService.getDeviceSimpleList(null, null).stream()
                .collect(Collectors.toMap(EnergyDeviceDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyCustomerDO> customerMap = customerService.getCustomerSimpleList().stream()
                .collect(Collectors.toMap(EnergyCustomerDO::getId, Function.identity(), (first, second) -> first));
        list.forEach(session -> {
            EnergyDeviceDO device = deviceMap.get(session.getDeviceId());
            if (device != null) {
                session.setDeviceName(device.getDeviceName());
                session.setDeviceNo(device.getDeviceNo());
            }
            EnergyCustomerDO customer = customerMap.get(session.getCustomerId());
            if (customer != null) {
                session.setCustomerName(customer.getName());
            }
        });
    }

}
