package cn.iocoder.yudao.module.energy.controller.eiot.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "EIOT - 电表实时数据接入 Request VO")
@Data
public class EnergyEiotRealtimeReqVO {

    @Schema(description = "网关唯一编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "网关序列号不能为空")
    private String gatewaySn;

    @Schema(description = "网关下的仪表编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "电表序列号不能为空")
    private String meterSn;

    @Schema(description = "仪表全平台唯一编号")
    private String meterNo;

    @Schema(description = "设备本地时间，格式 yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @Schema(description = "Unix 时间戳")
    private Long timestamp;

    @Schema(description = "数据类型：REALTIME/HISTORY")
    private String source;

    @Schema(description = "仪表状态：ONLINE/OFFLINE")
    private String state;

    @JsonProperty("Pa")
    private BigDecimal pa;

    @JsonProperty("Pb")
    private BigDecimal pb;

    @JsonProperty("Pc")
    private BigDecimal pc;

    @JsonProperty("Ua")
    private BigDecimal ua;

    @JsonProperty("Ub")
    private BigDecimal ub;

    @JsonProperty("Uc")
    private BigDecimal uc;

    @JsonProperty("Ia")
    private BigDecimal ia;

    @JsonProperty("Ib")
    private BigDecimal ib;

    @JsonProperty("Ic")
    private BigDecimal ic;

    @JsonProperty("P")
    private BigDecimal p;

    @JsonProperty("PF")
    private BigDecimal pf;

    @JsonProperty("EPI")
    private BigDecimal epi;

}
