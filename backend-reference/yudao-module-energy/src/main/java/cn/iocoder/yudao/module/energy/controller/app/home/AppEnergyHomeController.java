package cn.iocoder.yudao.module.energy.controller.app.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.controller.app.home.vo.AppEnergyHomeOverviewRespVO;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 移动储能首页")
@RestController
@RequestMapping("/energy/home")
@Validated
public class AppEnergyHomeController {

    @Resource
    private EnergyAppService energyAppService;

    @GetMapping("/overview")
    @Operation(summary = "获得首页概览")
    public CommonResult<AppEnergyHomeOverviewRespVO> getHomeOverview() {
        return success(energyAppService.getHomeOverview());
    }

}
