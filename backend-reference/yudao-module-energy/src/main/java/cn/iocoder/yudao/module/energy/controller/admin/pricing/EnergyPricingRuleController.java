package cn.iocoder.yudao.module.energy.controller.admin.pricing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRulePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRuleRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.pricing.vo.EnergyPricingRuleSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.pricing.EnergyPricingRuleDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.pricing.EnergyPricingRuleService;
import cn.iocoder.yudao.module.energy.service.project.EnergyProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动储能计费规则")
@RestController
@RequestMapping("/energy/pricing-rule")
@Validated
public class EnergyPricingRuleController {

    @Resource
    private EnergyPricingRuleService pricingRuleService;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;
    @Resource
    private EnergyDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建计费规则")
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:create')")
    public CommonResult<Long> createPricingRule(@Valid @RequestBody EnergyPricingRuleSaveReqVO createReqVO) {
        return success(pricingRuleService.createPricingRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新计费规则")
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:update')")
    public CommonResult<Boolean> updatePricingRule(@Valid @RequestBody EnergyPricingRuleSaveReqVO updateReqVO) {
        pricingRuleService.updatePricingRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计费规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:delete')")
    public CommonResult<Boolean> deletePricingRule(@RequestParam("id") Long id) {
        pricingRuleService.deletePricingRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得计费规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:query')")
    public CommonResult<EnergyPricingRuleRespVO> getPricingRule(@RequestParam("id") Long id) {
        EnergyPricingRuleDO pricingRule = pricingRuleService.validatePricingRuleExists(id);
        EnergyPricingRuleRespVO respVO = BeanUtils.toBean(pricingRule, EnergyPricingRuleRespVO.class);
        fillScopeNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得计费规则分页")
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:query')")
    public CommonResult<PageResult<EnergyPricingRuleRespVO>> getPricingRulePage(
            @Valid EnergyPricingRulePageReqVO pageReqVO) {
        PageResult<EnergyPricingRuleDO> pageResult = pricingRuleService.getPricingRulePage(pageReqVO);
        PageResult<EnergyPricingRuleRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyPricingRuleRespVO.class);
        fillScopeNames(respPageResult.getList());
        return success(respPageResult);
    }

    @GetMapping("/match")
    @Operation(summary = "匹配设备当前生效计费规则")
    @PreAuthorize("@ss.hasPermission('energy:pricing-rule:query')")
    public CommonResult<EnergyPricingRuleRespVO> matchPricingRule(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "billingTime", required = false) String billingTime) {
        EnergyPricingRuleDO pricingRule = pricingRuleService.getMatchedPricingRule(deviceId, billingTime);
        if (pricingRule == null) {
            return success(null);
        }
        EnergyPricingRuleRespVO respVO = BeanUtils.toBean(pricingRule, EnergyPricingRuleRespVO.class);
        fillScopeNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    private void fillScopeNames(List<EnergyPricingRuleRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyCustomerDO> customerMap = customerService.getCustomerSimpleList().stream()
                .collect(Collectors.toMap(EnergyCustomerDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyProjectDO> projectMap = projectService.getProjectSimpleList(null).stream()
                .collect(Collectors.toMap(EnergyProjectDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyDeviceDO> deviceMap = deviceService.getDeviceSimpleList(null, null).stream()
                .collect(Collectors.toMap(EnergyDeviceDO::getId, Function.identity(), (first, second) -> first));
        list.forEach(rule -> {
            EnergyCustomerDO customer = customerMap.get(rule.getCustomerId());
            if (customer != null) {
                rule.setCustomerName(customer.getName());
            }
            EnergyProjectDO project = projectMap.get(rule.getProjectId());
            if (project != null) {
                rule.setProjectName(project.getName());
            }
            EnergyDeviceDO device = deviceMap.get(rule.getDeviceId());
            if (device != null) {
                rule.setDeviceName(device.getDeviceName());
                rule.setDeviceNo(device.getDeviceNo());
            }
        });
    }

}
