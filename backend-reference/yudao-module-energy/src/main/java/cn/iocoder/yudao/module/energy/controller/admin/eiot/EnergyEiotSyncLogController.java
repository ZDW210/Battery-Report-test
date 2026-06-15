package cn.iocoder.yudao.module.energy.controller.admin.eiot;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.eiot.vo.EnergyEiotSyncLogPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.eiot.vo.EnergyEiotSyncLogRespVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.eiot.EnergyEiotSyncLogDO;
import cn.iocoder.yudao.module.energy.service.eiot.EnergyEiotService;
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

@Tag(name = "管理后台 - 移动储能 EIOT 同步日志")
@RestController
@RequestMapping("/energy/eiot-log")
@Validated
public class EnergyEiotSyncLogController {

    @Resource
    private EnergyEiotService eiotService;

    @GetMapping("/page")
    @Operation(summary = "获得 EIOT 同步日志分页")
    @PreAuthorize("@ss.hasPermission('energy:eiot-log:query')")
    public CommonResult<PageResult<EnergyEiotSyncLogRespVO>> getSyncLogPage(
            @Valid EnergyEiotSyncLogPageReqVO pageReqVO) {
        PageResult<EnergyEiotSyncLogDO> pageResult = eiotService.getSyncLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnergyEiotSyncLogRespVO.class));
    }

}
