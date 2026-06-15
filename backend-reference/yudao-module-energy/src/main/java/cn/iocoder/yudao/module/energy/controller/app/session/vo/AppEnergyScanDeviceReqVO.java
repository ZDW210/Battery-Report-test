package cn.iocoder.yudao.module.energy.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "用户 App - 扫码设备/充电桩校验 Request VO")
@Data
public class AppEnergyScanDeviceReqVO {

    @Schema(description = "扫码内容，可为 deviceId/deviceNo/meterNo 或带参数链接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "扫码内容不能为空")
    private String scanText;

    @Schema(description = "认证方式：WECHAT 或 CARD，默认 WECHAT")
    private String authType;

    @Schema(description = "刷卡卡号；authType=CARD 时使用")
    private String cardNo;

}
