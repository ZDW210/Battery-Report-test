package cn.iocoder.yudao.module.energy.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Schema(description = "Admin - Energy device control request")
@Data
public class EnergyDeviceControlReqVO {

    @Schema(description = "Device id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "deviceId cannot be empty")
    private Long deviceId;

    @Schema(description = "Control method: SWITCH/REFRESH", requiredMode = Schema.RequiredMode.REQUIRED, example = "SWITCH")
    @NotBlank(message = "method cannot be empty")
    private String method;

    @Schema(description = "Control value, SWITCH uses {\"Switch\":\"1\"} or {\"Switch\":\"0\"}")
    private Map<String, Object> value;

    @Schema(description = "Remark")
    private String remark;

}
