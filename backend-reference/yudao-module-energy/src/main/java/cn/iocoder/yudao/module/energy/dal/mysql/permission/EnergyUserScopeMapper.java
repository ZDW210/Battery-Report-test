package cn.iocoder.yudao.module.energy.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.permission.vo.EnergyUserScopePageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.permission.EnergyUserScopeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnergyUserScopeMapper extends BaseMapperX<EnergyUserScopeDO> {

    default PageResult<EnergyUserScopeDO> selectPage(EnergyUserScopePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyUserScopeDO>()
                .eqIfPresent(EnergyUserScopeDO::getUserId, reqVO.getUserId())
                .eqIfPresent(EnergyUserScopeDO::getUserType, reqVO.getUserType())
                .eqIfPresent(EnergyUserScopeDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(EnergyUserScopeDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(EnergyUserScopeDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(EnergyUserScopeDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyUserScopeDO::getId));
    }

    default List<EnergyUserScopeDO> selectEnabledListByUser(Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<EnergyUserScopeDO>()
                .eq(EnergyUserScopeDO::getUserId, userId)
                .eq(EnergyUserScopeDO::getUserType, userType)
                .eq(EnergyUserScopeDO::getStatus, 0));
    }

    default EnergyUserScopeDO selectByScope(Long userId, Integer userType, Long customerId, Long projectId, Long deviceId) {
        LambdaQueryWrapperX<EnergyUserScopeDO> wrapper = new LambdaQueryWrapperX<EnergyUserScopeDO>()
                .eq(EnergyUserScopeDO::getUserId, userId)
                .eq(EnergyUserScopeDO::getUserType, userType);
        if (customerId == null) {
            wrapper.isNull(EnergyUserScopeDO::getCustomerId);
        } else {
            wrapper.eq(EnergyUserScopeDO::getCustomerId, customerId);
        }
        if (projectId == null) {
            wrapper.isNull(EnergyUserScopeDO::getProjectId);
        } else {
            wrapper.eq(EnergyUserScopeDO::getProjectId, projectId);
        }
        if (deviceId == null) {
            wrapper.isNull(EnergyUserScopeDO::getDeviceId);
        } else {
            wrapper.eq(EnergyUserScopeDO::getDeviceId, deviceId);
        }
        return selectOne(wrapper);
    }

}
