package cn.iocoder.yudao.module.energy.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Admin - Energy App user update request")
@Data
public class EnergyAppUserUpdateReqVO {

    @Schema(description = "User id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User id cannot be empty")
    private Long id;

    @Schema(description = "Nickname")
    @Size(max = 64, message = "Nickname length cannot exceed 64")
    private String nickname;

    @Schema(description = "Mobile")
    @Size(max = 32, message = "Mobile length cannot exceed 32")
    private String mobile;

    @Schema(description = "Card number")
    @Size(max = 64, message = "Card number length cannot exceed 64")
    private String cardNo;

    @Schema(description = "Whether mini program management pages are enabled")
    private Boolean miniAdminEnabled;

    @Schema(description = "Status: 0 enabled, 1 disabled")
    @NotNull(message = "Status cannot be empty")
    private Integer status;

    @Schema(description = "Remark")
    @Size(max = 512, message = "Remark length cannot exceed 512")
    private String remark;

}
