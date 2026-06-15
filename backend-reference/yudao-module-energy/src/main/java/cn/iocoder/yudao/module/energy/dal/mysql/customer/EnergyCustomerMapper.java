package cn.iocoder.yudao.module.energy.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.customer.vo.EnergyCustomerPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customer.EnergyCustomerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 移动储能客户 Mapper。
 */
@Mapper
public interface EnergyCustomerMapper extends BaseMapperX<EnergyCustomerDO> {

    default PageResult<EnergyCustomerDO> selectPage(EnergyCustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyCustomerDO>()
                .likeIfPresent(EnergyCustomerDO::getName, reqVO.getName())
                .likeIfPresent(EnergyCustomerDO::getContactName, reqVO.getContactName())
                .eqIfPresent(EnergyCustomerDO::getRegion, reqVO.getRegion())
                .eqIfPresent(EnergyCustomerDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyCustomerDO::getId));
    }

    default List<EnergyCustomerDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<EnergyCustomerDO>()
                .orderByDesc(EnergyCustomerDO::getId));
    }

}
