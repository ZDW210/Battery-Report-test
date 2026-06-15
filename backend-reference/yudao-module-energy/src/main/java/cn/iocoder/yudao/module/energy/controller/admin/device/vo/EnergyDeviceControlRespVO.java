package cn.iocoder.yudao.module.energy.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Admin - Energy device control response")
@Data
@Builder
public class EnergyDeviceControlRespVO {

    @Schema(description = "Control log id", example = "1024")
    private Long controlLogId;

    @Schema(description = "Whether EIOT accepted the control command", example = "true")
    private Boolean success;

    @Schema(description = "Result message")
    private String message;

}
