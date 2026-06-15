package cn.iocoder.yudao.module.energy.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionStartReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.session.vo.EnergyChargeSessionStopReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.session.EnergyChargeSessionDO;

public interface EnergyChargeSessionService {

    PageResult<EnergyChargeSessionDO> getChargeSessionPage(EnergyChargeSessionPageReqVO pageReqVO);

    EnergyChargeSessionDO getChargeSession(Long id);

    EnergyChargeSessionDO startChargeSession(EnergyChargeSessionStartReqVO reqVO, Long operatorUserId);

    EnergyChargeSessionDO stopChargeSession(EnergyChargeSessionStopReqVO reqVO, Long operatorUserId);

    EnergyChargeSessionDO settleChargeSession(Long sessionId);

}
