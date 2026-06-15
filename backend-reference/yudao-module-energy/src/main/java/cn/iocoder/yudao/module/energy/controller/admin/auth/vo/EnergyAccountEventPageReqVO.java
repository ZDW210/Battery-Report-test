package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "Admin - account identification event page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyAccountEventPageReqVO extends PageParam {

    @Schema(description = "Event scene: VERIFY/DISCHARGE")
    private String eventScene;

    @Schema(description = "Auth type: WECHAT/CARD")
    private String authType;

    @Schema(description = "Whether the account is known and authorized")
    private Boolean accountKnown;

    @Schema(description = "Keyword for account/device/card/scan text")
    private String keyword;

    @Schema(description = "Created time range")
    private LocalDateTime[] createTime;

}
