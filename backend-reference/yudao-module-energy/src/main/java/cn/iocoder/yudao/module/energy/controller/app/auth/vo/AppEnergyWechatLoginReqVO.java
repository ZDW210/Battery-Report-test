package cn.iocoder.yudao.module.energy.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "User App - Energy WeChat mini program login request")
@Data
public class AppEnergyWechatLoginReqVO {

    @Schema(description = "WeChat mini program login code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "code cannot be empty")
    @Size(max = 128, message = "code length cannot exceed 128")
    private String code;

    @Schema(description = "WeChat nickname", example = "Acrel User")
    @Size(max = 64, message = "nickname length cannot exceed 64")
    private String nickname;

    @Schema(description = "WeChat avatar URL")
    @Size(max = 512, message = "avatar length cannot exceed 512")
    private String avatar;

}
