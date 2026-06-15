package cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - Energy vehicle response")
@Data
public class EnergyVehicleRespVO {

    @Schema(description = "ID", example = "1024")
    private Long id;

    @Schema(description = "Vehicle number", example = "VEH-DEMO-001")
    private String vehicleNo;

    @Schema(description = "Plate number", example = "TEST-A001")
    private String plateNo;

    @Schema(description = "QR code content", example = "QR-VEH-DEMO-001")
    private String qrCode;

    @Schema(description = "Bound device id", example = "1024")
    private Long deviceId;

    @Schema(description = "Device name")
    private String deviceName;

    @Schema(description = "Device number")
    private String deviceNo;

    @Schema(description = "Gateway SN")
    private String gatewaySn;

    @Schema(description = "Meter number")
    private String meterNo;

    @Schema(description = "Customer id")
    private Long customerId;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Project id")
    private Long projectId;

    @Schema(description = "Project name")
    private String projectName;

    @Schema(description = "Status: 0 enabled, 1 disabled")
    private Integer status;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

}
