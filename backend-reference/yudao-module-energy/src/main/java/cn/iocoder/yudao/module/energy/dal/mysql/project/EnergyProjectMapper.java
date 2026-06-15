package cn.iocoder.yudao.module.energy.dal.mysql.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.project.vo.EnergyProjectPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.project.EnergyProjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 移动储能项目 Mapper。
 */
@Mapper
public interface EnergyProjectMapper extends BaseMapperX<EnergyProjectDO> {

    default PageResult<EnergyProjectDO> selectPage(EnergyProjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnergyProjectDO>()
                .eqIfPresent(EnergyProjectDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(EnergyProjectDO::getName, reqVO.getName())
                .eqIfPresent(EnergyProjectDO::getCode, reqVO.getCode())
                .eqIfPresent(EnergyProjectDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyProjectDO::getId));
    }

    default EnergyProjectDO selectByCode(String code) {
        return selectOne(EnergyProjectDO::getCode, code);
    }

    default List<EnergyProjectDO> selectSimpleList(Long customerId) {
        return selectList(new LambdaQueryWrapperX<EnergyProjectDO>()
                .eqIfPresent(EnergyProjectDO::getCustomerId, customerId)
                .orderByDesc(EnergyProjectDO::getId));
    }

}
