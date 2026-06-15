package cn.iocoder.yudao.module.energy.controller.app.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "User App - Energy device response")
@Data
public class AppEnergyDeviceRespVO {

    private Long id;

    private String deviceName;

    private String deviceNo;

    private Integer deviceType;

    private String gatewaySn;

    private String meterSn;

    private String meterNo;

    private Long customerId;

    private Long projectId;

    private Integer status;

    private Integer runMode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal lastPower;

    private BigDecimal lastVoltage;

    private BigDecimal lastCurrent;

    private BigDecimal lastEpi;

    private LocalDateTime lastReadingTime;

    private String remark;

}
