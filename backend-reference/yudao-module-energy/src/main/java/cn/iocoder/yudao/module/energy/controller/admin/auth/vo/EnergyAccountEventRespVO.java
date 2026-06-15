package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin - account identification event response")
@Data
public class EnergyAccountEventRespVO {

    private Long id;

    private String eventScene;

    private String authType;

    private String scanText;

    private String cardNo;

    private Boolean accountKnown;

    private Long accountId;

    private String accountName;

    private String accountMobile;

    private Long deviceId;

    private String deviceNo;

    private String deviceName;

    private String meterNo;

    private String gatewaySn;

    private String meterSn;

    private Long customerId;

    private Long projectId;

    private String resultMessage;

    private LocalDateTime createTime;

}
