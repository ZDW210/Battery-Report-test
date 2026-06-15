package cn.iocoder.yudao.module.energy.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "User App - Energy login response")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppEnergyLoginRespVO {

    @Schema(description = "User id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "Access token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;

    @Schema(description = "Refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;

    @Schema(description = "Token expiry time", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

    @Schema(description = "App user profile", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {

        @Schema(description = "User id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx_openid_user")
        private String username;

        @Schema(description = "Display name", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信小程序用户")
        private String displayName;

        @Schema(description = "Mobile", example = "13800138000")
        private String mobile;

        @Schema(description = "Role", requiredMode = Schema.RequiredMode.REQUIRED, example = "app")
        private String role;

        @Schema(description = "Whether mini program management pages are enabled", example = "false")
        private Boolean miniAdminEnabled;

    }

}
