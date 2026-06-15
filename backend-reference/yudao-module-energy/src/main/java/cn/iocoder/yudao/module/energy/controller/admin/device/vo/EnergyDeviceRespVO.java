package cn.iocoder.yudao.module.energy.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能设备 Response VO")
@Data
public class EnergyDeviceRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ESS-001")
    private String deviceNo;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "移动储能柜 A")
    private String deviceName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer deviceType;

    @Schema(description = "网关序列号", example = "GW20260530001")
    private String gatewaySn;

    @Schema(description = "电表序列号", example = "METER-SN-001")
    private String meterSn;

    @Schema(description = "仪表编号", example = "METER-001")
    private String meterNo;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称", example = "安科瑞")
    private String customerName;

    @Schema(description = "项目编号", example = "2048")
    private Long projectId;

    @Schema(description = "项目名称", example = "上海园区")
    private String projectName;

    @Schema(description = "设备状态：0 在线，1 离线，2 故障，3 维护", example = "1")
    private Integer status;

    @Schema(description = "运行模式：0 充电，1 放电，2 待机，3 故障", example = "2")
    private Integer runMode;

    @Schema(description = "纬度", example = "31.230416")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "121.473701")
    private BigDecimal longitude;

    @Schema(description = "最新 SOC", example = "86.50")
    private BigDecimal lastSoc;

    @Schema(description = "最新 SOH", example = "98.20")
    private BigDecimal lastSoh;

    @Schema(description = "最新功率", example = "120.500")
    private BigDecimal lastPower;

    @Schema(description = "最新电压", example = "750.000")
    private BigDecimal lastVoltage;

    @Schema(description = "最新电流", example = "160.000")
    private BigDecimal lastCurrent;

    @Schema(description = "最新温度", example = "32.50")
    private BigDecimal lastTemp;

    @Schema(description = "最新采集时间")
    private LocalDateTime lastReadingTime;

    @Schema(description = "备注", example = "园区临时保电设备")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
