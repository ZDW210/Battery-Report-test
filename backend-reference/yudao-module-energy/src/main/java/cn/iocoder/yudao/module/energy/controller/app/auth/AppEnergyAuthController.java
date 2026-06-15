package cn.iocoder.yudao.module.energy.controller.app.auth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginReqVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginRespVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyWechatLoginReqVO;
import cn.iocoder.yudao.module.energy.service.auth.EnergyAppAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "User App - Energy auth")
@RestController
@RequestMapping("/energy/auth")
@Validated
public class AppEnergyAuthController {

    @Resource
    private EnergyAppAuthService appAuthService;

    @Value("${energy.app.password-login-enabled:false}")
    private boolean passwordLoginEnabled;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "Login by username and password")
    public CommonResult<AppEnergyLoginRespVO> login(@Valid @RequestBody AppEnergyLoginReqVO reqVO) {
        if (!passwordLoginEnabled) {
            throw exception0(FORBIDDEN.getCode(), "App password login is disabled");
        }
        return success(appAuthService.login(reqVO));
    }

    @PostMapping("/wechat-login")
    @PermitAll
    @Operation(summary = "Login by WeChat mini program code")
    public CommonResult<AppEnergyLoginRespVO> wechatLogin(@Valid @RequestBody AppEnergyWechatLoginReqVO reqVO) {
        return success(appAuthService.wechatLogin(reqVO));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current App user profile")
    public CommonResult<AppEnergyLoginRespVO.UserInfo> getProfile() {
        return success(appAuthService.getProfile());
    }

}
