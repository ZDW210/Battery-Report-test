package cn.iocoder.yudao.module.energy.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 移动储能设备列表 Request VO")
@Data
public class AppEnergyDeviceListReqVO {

    @Schema(description = "设备状态", example = "0")
    private Integer status;

    @Schema(description = "设备名称/编码关键字", example = "ESS")
    private String keyword;

}
