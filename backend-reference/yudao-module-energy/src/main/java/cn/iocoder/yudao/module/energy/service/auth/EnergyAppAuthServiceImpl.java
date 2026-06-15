package cn.iocoder.yudao.module.energy.service.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginReqVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginRespVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyWechatLoginReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAppUserDO;
import cn.iocoder.yudao.module.energy.dal.mysql.auth.EnergyAppUserMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.enums.oauth2.OAuth2ClientConstants;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class EnergyAppAuthServiceImpl implements EnergyAppAuthService {

    private static final String WECHAT_LOGIN_STATE = "energy-wechat-login";

    @Resource
    private EnergyAppUserMapper appUserMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private SocialUserApi socialUserApi;

    @Override
    public AppEnergyLoginRespVO login(AppEnergyLoginReqVO reqVO) {
        EnergyAppUserDO user = appUserMapper.selectByUsername(reqVO.getUsername());
        if (user == null || !passwordEncoder.matches(reqVO.getPassword(), user.getPassword())) {
            throw exception(APP_AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!CommonStatusEnum.isEnable(user.getStatus())) {
            throw exception(APP_AUTH_USER_DISABLED);
        }

        return createLoginResp(user);
    }

    @Override
    public AppEnergyLoginRespVO wechatLogin(AppEnergyWechatLoginReqVO reqVO) {
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByCode(UserTypeEnum.MEMBER.getValue(),
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), reqVO.getCode(), WECHAT_LOGIN_STATE);
        EnergyAppUserDO user = socialUser.getUserId() != null ? appUserMapper.selectById(socialUser.getUserId()) : null;
        if (user == null) {
            user = createWechatUser(reqVO, socialUser);
            socialUserApi.bindSocialUser(new SocialUserBindReqDTO(user.getId(), UserTypeEnum.MEMBER.getValue(),
                    SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), reqVO.getCode(), WECHAT_LOGIN_STATE));
        } else {
            updateWechatUserProfile(user, reqVO, socialUser);
        }
        if (!CommonStatusEnum.isEnable(user.getStatus())) {
            throw exception(APP_AUTH_USER_DISABLED);
        }
        return createLoginResp(user);
    }

    @Override
    public AppEnergyLoginRespVO.UserInfo getProfile() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        EnergyAppUserDO user = loginUser == null ? null : appUserMapper.selectById(loginUser.getId());
        if (user == null || !CommonStatusEnum.isEnable(user.getStatus())) {
            throw exception(APP_AUTH_USER_DISABLED);
        }
        return buildUserInfo(user);
    }

    private AppEnergyLoginRespVO createLoginResp(EnergyAppUserDO user) {
        updateLoginInfo(user.getId());
        OAuth2AccessTokenDO accessToken = oauth2TokenService.createAccessToken(user.getId(),
                UserTypeEnum.MEMBER.getValue(), OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
        return AppEnergyLoginRespVO.builder()
                .userId(user.getId())
                .accessToken(accessToken.getAccessToken())
                .refreshToken(accessToken.getRefreshToken())
                .expiresTime(accessToken.getExpiresTime())
                .user(buildUserInfo(user))
                .build();
    }

    private AppEnergyLoginRespVO.UserInfo buildUserInfo(EnergyAppUserDO user) {
        boolean miniAdminEnabled = Boolean.TRUE.equals(user.getMiniAdminEnabled());
        return AppEnergyLoginRespVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getNickname())
                .mobile(user.getMobile())
                .role(miniAdminEnabled ? "manager" : "driver")
                .miniAdminEnabled(miniAdminEnabled)
                .build();
    }

    private EnergyAppUserDO createWechatUser(AppEnergyWechatLoginReqVO reqVO, SocialUserRespDTO socialUser) {
        EnergyAppUserDO user = EnergyAppUserDO.builder()
                .username(buildWechatUsername(socialUser.getOpenid()))
                .password(passwordEncoder.encode(socialUser.getOpenid()))
                .nickname(buildWechatNickname(reqVO, socialUser))
                .miniAdminEnabled(false)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .remark("WeChat mini program auto-created user")
                .build();
        appUserMapper.insert(user);
        return user;
    }

    private void updateWechatUserProfile(EnergyAppUserDO user, AppEnergyWechatLoginReqVO reqVO, SocialUserRespDTO socialUser) {
        String nickname = buildWechatNickname(reqVO, socialUser);
        if (StrUtil.isNotBlank(nickname) && !StrUtil.equals(nickname, user.getNickname())) {
            appUserMapper.updateById(new EnergyAppUserDO()
                    .setId(user.getId())
                    .setNickname(nickname));
            user.setNickname(nickname);
        }
    }

    private String buildWechatUsername(String openid) {
        return "wx_" + openid;
    }

    private String buildWechatNickname(AppEnergyWechatLoginReqVO reqVO, SocialUserRespDTO socialUser) {
        return StrUtil.blankToDefault(reqVO.getNickname(),
                StrUtil.blankToDefault(socialUser.getNickname(), "微信小程序用户"));
    }

    private void updateLoginInfo(Long userId) {
        appUserMapper.updateById(new EnergyAppUserDO()
                .setId(userId)
                .setLoginIp(ServletUtils.getClientIP())
                .setLoginDate(LocalDateTime.now()));
    }

}
