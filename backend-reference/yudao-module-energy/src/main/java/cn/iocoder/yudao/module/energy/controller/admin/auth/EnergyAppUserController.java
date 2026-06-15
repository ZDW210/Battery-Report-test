package cn.iocoder.yudao.module.energy.controller.admin.auth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAppUserPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAppUserRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAppUserSimpleRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAppUserUpdateReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAppUserDO;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAppUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动储能 App 用户")
@RestController
@RequestMapping("/energy/app-user")
@Validated
public class EnergyAppUserController {

    @Resource
    private EnergyAppUserMapper appUserMapper;

    @GetMapping("/page")
    @Operation(summary = "Get App user page")
    @PreAuthorize("@ss.hasPermission('energy:app-user:query')")
    public CommonResult<PageResult<EnergyAppUserRespVO>> getAppUserPage(@Valid EnergyAppUserPageReqVO pageReqVO) {
        PageResult<EnergyAppUserDO> pageResult = appUserMapper.selectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnergyAppUserRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "Get App user")
    @Parameter(name = "id", description = "ID", required = true)
    @PreAuthorize("@ss.hasPermission('energy:app-user:query')")
    public CommonResult<EnergyAppUserRespVO> getAppUser(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(appUserMapper.selectById(id), EnergyAppUserRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "Update App user")
    @PreAuthorize("@ss.hasPermission('energy:app-user:update')")
    public CommonResult<Boolean> updateAppUser(@Valid @RequestBody EnergyAppUserUpdateReqVO updateReqVO) {
        appUserMapper.updateById(BeanUtils.toBean(updateReqVO, EnergyAppUserDO.class));
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得 App 用户精简列表")
    @PreAuthorize("@ss.hasPermission('energy:user-scope:query')")
    public CommonResult<List<EnergyAppUserSimpleRespVO>> getAppUserSimpleList(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<EnergyAppUserDO> list = appUserMapper.selectEnabledSimpleList(keyword);
        return success(BeanUtils.toBean(list, EnergyAppUserSimpleRespVO.class));
    }

}
