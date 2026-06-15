package cn.iocoder.yudao.module.energy.controller.admin.permission;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopePageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopeRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopeSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAppUserDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.device.EnergyDeviceDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.permission.EnergyUserScopeDO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAppUserMapper;
import cn.iocoder.yudao.module.energy.service.customer.EnergyCustomerService;
import cn.iocoder.yudao.module.energy.service.device.EnergyDeviceService;
import cn.iocoder.yudao.module.energy.service.permission.EnergyUserScopeService;
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

@Tag(name = "管理后台 - 移动储能用户授权")
@RestController
@RequestMapping("/energy/user-scope")
@Validated
public class EnergyUserScopeController {

    @Resource
    private EnergyUserScopeService userScopeService;
    @Resource
    private EnergyCustomerService customerService;
    @Resource
    private EnergyProjectService projectService;
    @Resource
    private EnergyDeviceService deviceService;
    @Resource
    private EnergyAppUserMapper appUserMapper;

    @PostMapping("/create")
    @Operation(summary = "创建用户授权")
    @PreAuthorize("@ss.hasPermission('energy:user-scope:create')")
    public CommonResult<Long> createUserScope(@Valid @RequestBody EnergyUserScopeSaveReqVO createReqVO) {
        return success(userScopeService.createUserScope(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户授权")
    @PreAuthorize("@ss.hasPermission('energy:user-scope:update')")
    public CommonResult<Boolean> updateUserScope(@Valid @RequestBody EnergyUserScopeSaveReqVO updateReqVO) {
        userScopeService.updateUserScope(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户授权")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('energy:user-scope:delete')")
    public CommonResult<Boolean> deleteUserScope(@RequestParam("id") Long id) {
        userScopeService.deleteUserScope(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户授权")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('energy:user-scope:query')")
    public CommonResult<EnergyUserScopeRespVO> getUserScope(@RequestParam("id") Long id) {
        EnergyUserScopeDO userScope = userScopeService.getUserScope(id);
        EnergyUserScopeRespVO respVO = BeanUtils.toBean(userScope, EnergyUserScopeRespVO.class);
        fillResourceNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户授权分页")
    @PreAuthorize("@ss.hasPermission('energy:user-scope:query')")
    public CommonResult<PageResult<EnergyUserScopeRespVO>> getUserScopePage(@Valid EnergyUserScopePageReqVO pageReqVO) {
        PageResult<EnergyUserScopeDO> pageResult = userScopeService.getUserScopePage(pageReqVO);
        PageResult<EnergyUserScopeRespVO> respPageResult = BeanUtils.toBean(pageResult, EnergyUserScopeRespVO.class);
        fillResourceNames(respPageResult.getList());
        return success(respPageResult);
    }

    private void fillResourceNames(List<EnergyUserScopeRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, EnergyCustomerDO> customerMap = customerService.getCustomerSimpleList().stream()
                .collect(Collectors.toMap(EnergyCustomerDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyProjectDO> projectMap = projectService.getProjectSimpleList(null).stream()
                .collect(Collectors.toMap(EnergyProjectDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyDeviceDO> deviceMap = deviceService.getDeviceSimpleList(null, null).stream()
                .collect(Collectors.toMap(EnergyDeviceDO::getId, Function.identity(), (first, second) -> first));
        Map<Long, EnergyAppUserDO> appUserMap = appUserMapper.selectSimpleList(null).stream()
                .collect(Collectors.toMap(EnergyAppUserDO::getId, Function.identity(), (first, second) -> first));

        list.forEach(userScope -> {
            EnergyAppUserDO appUser = appUserMap.get(userScope.getUserId());
            if (appUser != null) {
                userScope.setUserUsername(appUser.getUsername());
                userScope.setUserNickname(appUser.getNickname());
                userScope.setUserMobile(appUser.getMobile());
            }
            EnergyCustomerDO customer = customerMap.get(userScope.getCustomerId());
            if (customer != null) {
                userScope.setCustomerName(customer.getName());
            }
            EnergyProjectDO project = projectMap.get(userScope.getProjectId());
            if (project != null) {
                userScope.setProjectName(project.getName());
            }
            EnergyDeviceDO device = deviceMap.get(userScope.getDeviceId());
            if (device != null) {
                userScope.setDeviceName(device.getDeviceName());
                userScope.setDeviceNo(device.getDeviceNo());
            }
        });
    }

}
