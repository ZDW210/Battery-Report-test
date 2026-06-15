package cn.iocoder.yudao.module.energy.service.customeraccount;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountMenuOptionRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountSaveReqVO;

import java.util.List;

public interface EnergyCustomerAccountService {

    Long createCustomerAccount(EnergyCustomerAccountSaveReqVO createReqVO);

    void updateCustomerAccount(EnergyCustomerAccountSaveReqVO updateReqVO);

    void resetPassword(Long id, String password);

    EnergyCustomerAccountRespVO getCustomerAccount(Long id);

    PageResult<EnergyCustomerAccountRespVO> getCustomerAccountPage(EnergyCustomerAccountPageReqVO pageReqVO);

    List<EnergyCustomerAccountMenuOptionRespVO> getVisibleMenuOptions();

}
