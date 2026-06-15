package cn.iocoder.yudao.module.energy.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 移动储能设备新增/修改 Request VO")
@Data
public class EnergyDeviceSaveReqVO {

    @Schema(description = "设备编号", example = "1024")
    private Long id;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ESS-001")
    @NotEmpty(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码长度不能超过 64 个字符")
    private String deviceNo;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "移动储能柜 A")
    @NotEmpty(message = "设备名称不能为空")
    @Size(max = 128, message = "设备名称长度不能超过 128 个字符")
    private String deviceName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "网关序列号", requiredMode = Schema.RequiredMode.REQUIRED, example = "GW20260530001")
    @NotEmpty(message = "网关序列号不能为空")
    @Size(max = 64, message = "网关序列号长度不能超过 64 个字符")
    private String gatewaySn;

    @Schema(description = "电表序列号", requiredMode = Schema.RequiredMode.REQUIRED, example = "METER-SN-001")
    @NotEmpty(message = "电表序列号不能为空")
    @Size(max = 64, message = "电表序列号长度不能超过 64 个字符")
    private String meterSn;

    @Schema(description = "仪表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "METER-001")
    @NotEmpty(message = "仪表编号不能为空")
    @Size(max = 128, message = "仪表编号长度不能超过 128 个字符")
    private String meterNo;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "所属客户不能为空")
    private Long customerId;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "项目场站不能为空")
    private Long projectId;

    @Schema(description = "设备状态：0 在线，1 离线，2 故障，3 维护", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备状态不能为空")
    private Integer status;

    @Schema(description = "运行模式：0 充电，1 放电，2 待机，3 故障", example = "2")
    private Integer runMode;

    @Schema(description = "纬度", example = "31.230416")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "121.473701")
    private BigDecimal longitude;

    @Schema(description = "备注", example = "园区临时保电设备")
    @Size(max = 512, message = "备注长度不能超过 512 个字符")
    private String remark;

}
