package cn.iocoder.yudao.module.energy.controller.admin.customeraccount;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.*;
import cn.iocoder.yudao.module.energy.service.customeraccount.EnergyCustomerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动储能客户账号权限")
@RestController
@RequestMapping("/energy/customer-account")
@Validated
public class EnergyCustomerAccountController {

    @Resource
    private EnergyCustomerAccountService customerAccountService;

    @PostMapping("/create")
    @Operation(summary = "创建客户老板账号")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:create')")
    public CommonResult<Long> createCustomerAccount(@Valid @RequestBody EnergyCustomerAccountSaveReqVO createReqVO) {
        return success(customerAccountService.createCustomerAccount(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户老板账号")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:update')")
    public CommonResult<Boolean> updateCustomerAccount(@Valid @RequestBody EnergyCustomerAccountSaveReqVO updateReqVO) {
        customerAccountService.updateCustomerAccount(updateReqVO);
        return success(true);
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置客户老板账号密码")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:reset-password')")
    public CommonResult<Boolean> resetPassword(@Valid @RequestBody EnergyCustomerAccountResetPasswordReqVO reqVO) {
        customerAccountService.resetPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户老板账号")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:query')")
    public CommonResult<EnergyCustomerAccountRespVO> getCustomerAccount(@RequestParam("id") Long id) {
        return success(customerAccountService.getCustomerAccount(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户老板账号分页")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:query')")
    public CommonResult<PageResult<EnergyCustomerAccountRespVO>> getCustomerAccountPage(
            @Valid EnergyCustomerAccountPageReqVO pageReqVO) {
        return success(customerAccountService.getCustomerAccountPage(pageReqVO));
    }

    @GetMapping("/menu-options")
    @Operation(summary = "获得可开放板块列表")
    @PreAuthorize("@ss.hasPermission('energy:customer-account:query')")
    public CommonResult<List<EnergyCustomerAccountMenuOptionRespVO>> getVisibleMenuOptions() {
        return success(customerAccountService.getVisibleMenuOptions());
    }

}
