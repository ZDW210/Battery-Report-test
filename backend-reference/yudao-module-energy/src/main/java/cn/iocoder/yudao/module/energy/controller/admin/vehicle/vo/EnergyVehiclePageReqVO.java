package cn.iocoder.yudao.module.energy.controller.admin.vehicle.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - Energy vehicle page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyVehiclePageReqVO extends PageParam {

    @Schema(description = "Vehicle number", example = "VEH-DEMO-001")
    private String vehicleNo;

    @Schema(description = "Plate number", example = "TEST-A001")
    private String plateNo;

    @Schema(description = "QR code content", example = "QR-VEH-DEMO-001")
    private String qrCode;

    @Schema(description = "Bound device id", example = "1024")
    private Long deviceId;

    @Schema(description = "Customer id", example = "1024")
    private Long customerId;

    @Schema(description = "Project id", example = "2048")
    private Long projectId;

    @Schema(description = "Status: 0 enabled, 1 disabled", example = "0")
    private Integer status;

}
