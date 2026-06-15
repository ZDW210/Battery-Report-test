package cn.iocoder.yudao.module.energy.controller.admin.customer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
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

@Tag(name = "管理后台 - 移动储能客户")
@RestController
@RequestMapping("/energy/customer")
@Validated
public class EnergyCustomerController {

    @Resource
    private EnergyCustomerService customerService;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('energy:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody EnergyCustomerSaveReqVO createReqVO) {
        return success(customerService.createCustomer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('energy:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody EnergyCustomerSaveReqVO updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('energy:customer:delete')")
    public CommonResult<Boolean> deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:customer:query')")
    public CommonResult<EnergyCustomerRespVO> getCustomer(@RequestParam("id") Long id) {
        EnergyCustomerDO customer = customerService.getCustomer(id);
        return success(BeanUtils.toBean(customer, EnergyCustomerRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('energy:customer:query')")
    public CommonResult<PageResult<EnergyCustomerRespVO>> getCustomerPage(@Valid EnergyCustomerPageReqVO pageReqVO) {
        PageResult<EnergyCustomerDO> pageResult = customerService.getCustomerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnergyCustomerRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得客户精简列表", description = "主要用于前端下拉")
    @PreAuthorize("@ss.hasPermission('energy:customer:query')")
    public CommonResult<List<EnergyCustomerRespVO>> getCustomerSimpleList() {
        List<EnergyCustomerDO> list = customerService.getCustomerSimpleList();
        return success(BeanUtils.toBean(list, EnergyCustomerRespVO.class));
    }

}
