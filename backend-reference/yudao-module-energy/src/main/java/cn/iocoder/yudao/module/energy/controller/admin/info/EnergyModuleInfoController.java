package cn.iocoder.yudao.module.energy.controller.admin.info;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.controller.admin.info.vo.EnergyModuleInfoRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Energy module info")
@RestController
@RequestMapping("/energy/module-info")
@Validated
public class EnergyModuleInfoController {

    @GetMapping("/runtime")
    @Operation(summary = "Get energy module runtime baseline")
    public CommonResult<EnergyModuleInfoRespVO> getRuntime() {
        return success(new EnergyModuleInfoRespVO(
                "yudao-module-energy",
                "0.1.0",
                "BOOTSTRAPPED",
                List.of("device", "alarm", "customer", "billing", "charge-record")));
    }

}
