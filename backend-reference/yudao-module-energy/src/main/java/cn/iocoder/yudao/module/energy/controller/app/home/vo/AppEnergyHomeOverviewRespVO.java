package cn.iocoder.yudao.module.energy.controller.app.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 App - 移动储能首页概览 Response VO")
@Data
@Builder
public class AppEnergyHomeOverviewRespVO {

    @Schema(description = "设备总数", example = "12")
    private Long deviceCount;

    @Schema(description = "在线设备数", example = "10")
    private Long onlineCount;

    @Schema(description = "故障设备数", example = "1")
    private Long faultCount;

    @Schema(description = "未确认告警数", example = "3")
    private Long unackedAlarmCount;

    @Schema(description = "今日充电量", example = "120.5")
    private BigDecimal todayChargeEnergy;

    @Schema(description = "今日放电量", example = "86.3")
    private BigDecimal todayDischargeEnergy;

    @Schema(description = "当前功率", example = "35.2")
    private BigDecimal currentPower;

}
