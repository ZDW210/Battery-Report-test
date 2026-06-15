package cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能实时采集数据 Response VO")
@Data
public class EnergyTelemetryRespVO {

    @Schema(description = "采集数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "设备编号", example = "202605310003")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备编码")
    private String deviceNo;

    @Schema(description = "网关序列号")
    private String gatewaySn;

    @Schema(description = "仪表序列号")
    private String meterSn;

    @Schema(description = "仪表全平台编号")
    private String meterNo;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

    @Schema(description = "Unix 时间戳")
    private Long timestamp;

    @Schema(description = "数据类型：REALTIME/HISTORY")
    private String source;

    @Schema(description = "仪表状态：ONLINE/OFFLINE")
    private String state;

    @Schema(description = "A 相有功功率")
    private BigDecimal pa;

    @Schema(description = "B 相有功功率")
    private BigDecimal pb;

    @Schema(description = "C 相有功功率")
    private BigDecimal pc;

    @Schema(description = "A 相电压")
    private BigDecimal ua;

    @Schema(description = "B 相电压")
    private BigDecimal ub;

    @Schema(description = "C 相电压")
    private BigDecimal uc;

    @Schema(description = "A 相电流")
    private BigDecimal ia;

    @Schema(description = "B 相电流")
    private BigDecimal ib;

    @Schema(description = "C 相电流")
    private BigDecimal ic;

    @Schema(description = "总有功功率")
    private BigDecimal p;

    @Schema(description = "总功率因数")
    private BigDecimal pf;

    @Schema(description = "正向总有功电能")
    private BigDecimal epi;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
