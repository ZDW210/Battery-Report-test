package cn.iocoder.yudao.module.energy.dal.mysql.customeraccount;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.customeraccount.vo.EnergyCustomerAccountPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.customeraccount.EnergyCustomerAccountDO;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.annotations.Mapper;

/**
 * Fleet owner web account Mapper.
 */
@Mapper
public interface EnergyCustomerAccountMapper extends BaseMapperX<EnergyCustomerAccountDO> {

    default PageResult<EnergyCustomerAccountDO> selectPage(EnergyCustomerAccountPageReqVO reqVO) {
        LambdaQueryWrapperX<EnergyCustomerAccountDO> query = new LambdaQueryWrapperX<EnergyCustomerAccountDO>()
                .eqIfPresent(EnergyCustomerAccountDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyCustomerAccountDO::getStatus, reqVO.getStatus());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            query.and(wrapper -> wrapper
                        .like(EnergyCustomerAccountDO::getUsername, reqVO.getKeyword())
                        .or()
                        .like(EnergyCustomerAccountDO::getNickname, reqVO.getKeyword())
                        .or()
                        .like(EnergyCustomerAccountDO::getMobile, reqVO.getKeyword()));
        }
        query.orderByDesc(EnergyCustomerAccountDO::getId);
        return selectPage(reqVO, query);
    }

}
