package cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Admin - Energy vehicle create/update request")
@Data
public class EnergyVehicleSaveReqVO {

    @Schema(description = "ID", example = "1024")
    private Long id;

    @Schema(description = "Vehicle number", requiredMode = Schema.RequiredMode.REQUIRED, example = "VEH-DEMO-001")
    @NotEmpty(message = "Vehicle number cannot be empty")
    @Size(max = 64, message = "Vehicle number length cannot exceed 64")
    private String vehicleNo;

    @Schema(description = "Plate number", requiredMode = Schema.RequiredMode.REQUIRED, example = "TEST-A001")
    @NotEmpty(message = "Plate number cannot be empty")
    @Size(max = 64, message = "Plate number length cannot exceed 64")
    private String plateNo;

    @Schema(description = "QR code content", requiredMode = Schema.RequiredMode.REQUIRED, example = "QR-VEH-DEMO-001")
    @NotEmpty(message = "QR code cannot be empty")
    @Size(max = 128, message = "QR code length cannot exceed 128")
    private String qrCode;

    @Schema(description = "Bound device id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Bound device cannot be empty")
    private Long deviceId;

    @Schema(description = "Status: 0 enabled, 1 disabled", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "Status cannot be empty")
    private Integer status;

    @Schema(description = "Remark")
    @Size(max = 512, message = "Remark length cannot exceed 512")
    private String remark;

}
