package cn.iocoder.yudao.module.energy.controller.app.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceListReqVO;
import cn.iocoder.yudao.module.energy.controller.app.device.vo.AppEnergyDeviceRespVO;
import cn.iocoder.yudao.module.energy.service.app.EnergyAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 移动储能设备")
@RestController
@RequestMapping("/energy/device")
@Validated
public class AppEnergyDeviceController {

    @Resource
    private EnergyAppService energyAppService;

    @GetMapping("/list")
    @Operation(summary = "获得设备列表")
    public CommonResult<List<AppEnergyDeviceRespVO>> getDeviceList(@Valid AppEnergyDeviceListReqVO reqVO) {
        return success(energyAppService.getDeviceList(reqVO));
    }

}
