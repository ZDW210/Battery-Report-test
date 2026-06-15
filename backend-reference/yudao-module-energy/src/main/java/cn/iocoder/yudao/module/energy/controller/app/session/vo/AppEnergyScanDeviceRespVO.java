package cn.iocoder.yudao.module.energy.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 扫码设备/充电桩校验 Response VO")
@Data
@Builder
public class AppEnergyScanDeviceRespVO {

    @Schema(description = "设备编号")
    private Long deviceId;

    @Schema(description = "账户是否已识别且拥有当前设备权限")
    private Boolean accountKnown;

    @Schema(description = "账户识别方式：WECHAT 或 CARD")
    private String accountType;

    @Schema(description = "账户编号")
    private Long accountId;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账户手机号")
    private String accountMobile;

    @Schema(description = "卡号")
    private String cardNo;

    @Schema(description = "识别结果提示")
    private String message;

    @Schema(description = "兼容旧车辆绑定编号")
    private Long vehicleId;

    @Schema(description = "兼容旧车辆编码")
    private String vehicleNo;

    @Schema(description = "兼容旧车牌号")
    private String plateNo;

    @Schema(description = "二维码编码")
    private String qrCode;

    @Schema(description = "设备编码")
    private String deviceNo;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "仪表编号")
    private String meterNo;

    @Schema(description = "网关编号")
    private String gatewaySn;

    @Schema(description = "电表编号")
    private String meterSn;

    @Schema(description = "客户编号")
    private Long customerId;

    @Schema(description = "项目编号")
    private Long projectId;

    @Schema(description = "设备状态")
    private Integer status;

    @Schema(description = "当前功率")
    private BigDecimal lastPower;

    @Schema(description = "最近采集时间")
    private LocalDateTime lastReadingTime;

}
