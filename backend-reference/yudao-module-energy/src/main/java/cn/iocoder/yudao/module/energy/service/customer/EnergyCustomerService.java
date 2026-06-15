package cn.iocoder.yudao.module.energy.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;

import java.util.List;

/**
 * 移动储能客户 Service 接口。
 */
public interface EnergyCustomerService {

    Long createCustomer(EnergyCustomerSaveReqVO createReqVO);

    void updateCustomer(EnergyCustomerSaveReqVO updateReqVO);

    void deleteCustomer(Long id);

    EnergyCustomerDO getCustomer(Long id);

    PageResult<EnergyCustomerDO> getCustomerPage(EnergyCustomerPageReqVO pageReqVO);

    List<EnergyCustomerDO> getCustomerSimpleList();

    EnergyCustomerDO validateCustomerExists(Long id);

}
