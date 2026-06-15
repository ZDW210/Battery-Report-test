package cn.iocoder.yudao.module.energy.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "User App - Energy login request")
@Data
public class AppEnergyLoginReqVO {

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx_openid_user")
    @NotEmpty(message = "username cannot be empty")
    @Size(max = 64, message = "username length cannot exceed 64")
    private String username;

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, example = "temporary-password")
    @NotEmpty(message = "password cannot be empty")
    @Size(max = 128, message = "password length cannot exceed 128")
    private String password;

}
