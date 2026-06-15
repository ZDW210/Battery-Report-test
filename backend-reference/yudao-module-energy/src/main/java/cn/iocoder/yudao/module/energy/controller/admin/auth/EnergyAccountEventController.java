package cn.iocoder.yudao.module.energy.controller.admin.auth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAccountEventPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAccountEventRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAccountEventDO;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAccountEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - mobile energy account identification events")
@RestController
@RequestMapping("/energy/account-event")
@Validated
public class EnergyAccountEventController {

    @Resource
    private EnergyAccountEventMapper accountEventMapper;

    @GetMapping("/page")
    @Operation(summary = "Get account identification event page")
    @PreAuthorize("@ss.hasPermission('energy:account-event:query')")
    public CommonResult<PageResult<EnergyAccountEventRespVO>> getAccountEventPage(
            @Valid EnergyAccountEventPageReqVO pageReqVO) {
        PageResult<EnergyAccountEventDO> pageResult = accountEventMapper.selectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnergyAccountEventRespVO.class));
    }

}
