package cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 移动储能逐日极值 Request VO")
@Data
public class EnergyTelemetryDailyStatReqVO {

    @Schema(description = "设备编号", example = "202605310003")
    private Long deviceId;

    @Schema(description = "网关序列号", example = "12207013690004")
    private String gatewaySn;

    @Schema(description = "仪表全平台编号", example = "12207013690004_12207013690004")
    private String meterNo;

    @Schema(description = "统计指标：pa/pb/pc/p/ua/ub/uc/ia/ib/ic/pf/epi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "统计指标不能为空")
    private String metric;

    @Schema(description = "采集时间范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采集时间范围不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] collectTime;

}
