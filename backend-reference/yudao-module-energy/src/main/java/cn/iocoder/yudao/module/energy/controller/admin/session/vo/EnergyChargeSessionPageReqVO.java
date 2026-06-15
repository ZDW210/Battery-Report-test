package cn.iocoder.yudao.module.energy.controller.admin.session.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 移动储能充放电会话分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyChargeSessionPageReqVO extends PageParam {

    @Schema(description = "会话编号", example = "SES202606050001")
    private String sessionNo;

    @Schema(description = "设备编号", example = "202605310003")
    private Long deviceId;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "会话类型：0 充电，1 放电", example = "0")
    private Integer sessionType;

    @Schema(description = "状态：0 进行中，1 已结束，2 异常，3 已结算", example = "0")
    private Integer status;

    @Schema(description = "开始时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

}
