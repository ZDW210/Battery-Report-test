package cn.iocoder.yudao.module.energy.service.auth;

import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginReqVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyLoginRespVO;
import cn.iocoder.yudao.module.energy.controller.app.auth.vo.AppEnergyWechatLoginReqVO;
import jakarta.validation.Valid;

public interface EnergyAppAuthService {

    AppEnergyLoginRespVO login(@Valid AppEnergyLoginReqVO reqVO);

    AppEnergyLoginRespVO wechatLogin(@Valid AppEnergyWechatLoginReqVO reqVO);

    AppEnergyLoginRespVO.UserInfo getProfile();

}
