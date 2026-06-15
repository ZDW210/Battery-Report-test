package cn.iocoder.yudao.module.energy.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 移动储能充放电会话 Response VO")
@Data
public class EnergyChargeSessionRespVO {

    @Schema(description = "会话编号", example = "1900501")
    private Long id;

    @Schema(description = "会话业务编号", example = "SES202606050001")
    private String sessionNo;

    @Schema(description = "设备编号", example = "202605310003")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备编码")
    private String deviceNo;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "计费规则编号", example = "1024")
    private Long pricingRuleId;

    @Schema(description = "会话类型：0 充电，1 放电")
    private Integer sessionType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "开始电量读数")
    private BigDecimal startEnergy;

    @Schema(description = "结束电量读数")
    private BigDecimal endEnergy;

    @Schema(description = "总电量")
    private BigDecimal totalEnergy;

    @Schema(description = "时长分钟")
    private Integer durationMinutes;

    @Schema(description = "电量费用")
    private BigDecimal energyFee;

    @Schema(description = "时间费用")
    private BigDecimal timeFee;

    @Schema(description = "总费用")
    private BigDecimal totalFee;

    @Schema(description = "状态：0 进行中，1 已结束，2 异常，3 已结算")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
