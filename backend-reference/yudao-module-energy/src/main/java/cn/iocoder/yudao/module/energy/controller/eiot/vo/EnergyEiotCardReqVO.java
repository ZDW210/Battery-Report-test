package cn.iocoder.yudao.module.energy.controller.eiot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "EIOT - 刷卡识别 Request VO")
@Data
public class EnergyEiotCardReqVO {

    @Schema(description = "设备识别内容，可为 deviceId/deviceNo/meterNo 或带参数链接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备识别内容不能为空")
    private String scanText;

    @Schema(description = "刷卡卡号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "刷卡卡号不能为空")
    private String cardNo;

}
