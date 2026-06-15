package cn.iocoder.yudao.module.energy.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerPageReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerSaveReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import cn.iocoder.yudao.module.energy.dal.mysql.customer.EnergyCustomerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.energy.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

/**
 * 移动储能客户 Service 实现类。
 */
@Service
@Validated
public class EnergyCustomerServiceImpl implements EnergyCustomerService {

    @Resource
    private EnergyCustomerMapper customerMapper;

    @Override
    public Long createCustomer(EnergyCustomerSaveReqVO createReqVO) {
        EnergyCustomerDO customer = BeanUtils.toBean(createReqVO, EnergyCustomerDO.class);
        customerMapper.insert(customer);
        return customer.getId();
    }

    @Override
    public void updateCustomer(EnergyCustomerSaveReqVO updateReqVO) {
        validateCustomerExists(updateReqVO.getId());
        EnergyCustomerDO updateObj = BeanUtils.toBean(updateReqVO, EnergyCustomerDO.class);
        customerMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomer(Long id) {
        validateCustomerExists(id);
        customerMapper.deleteById(id);
    }

    @Override
    public EnergyCustomerDO getCustomer(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public PageResult<EnergyCustomerDO> getCustomerPage(EnergyCustomerPageReqVO pageReqVO) {
        return customerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EnergyCustomerDO> getCustomerSimpleList() {
        return customerMapper.selectSimpleList();
    }

    @Override
    public EnergyCustomerDO validateCustomerExists(Long id) {
        EnergyCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        return customer;
    }

}
