package cn.iocoder.yudao.module.energy.dal.mysql.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAppUserPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAppUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnergyAppUserMapper extends BaseMapperX<EnergyAppUserDO> {

    default PageResult<EnergyAppUserDO> selectPage(EnergyAppUserPageReqVO reqVO) {
        LambdaQueryWrapperX<EnergyAppUserDO> wrapper = new LambdaQueryWrapperX<EnergyAppUserDO>();
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(query -> query
                        .like(EnergyAppUserDO::getUsername, reqVO.getKeyword())
                        .or()
                        .like(EnergyAppUserDO::getNickname, reqVO.getKeyword())
                        .or()
                        .like(EnergyAppUserDO::getMobile, reqVO.getKeyword())
                        .or()
                        .like(EnergyAppUserDO::getCardNo, reqVO.getKeyword()));
        }
        return selectPage(reqVO, wrapper
                .eqIfPresent(EnergyAppUserDO::getStatus, reqVO.getStatus())
                .orderByDesc(EnergyAppUserDO::getCreateTime));
    }

    default EnergyAppUserDO selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapperX<EnergyAppUserDO>()
                .eq(EnergyAppUserDO::getUsername, username));
    }

    default EnergyAppUserDO selectByCardNo(String cardNo) {
        return selectOne(new LambdaQueryWrapperX<EnergyAppUserDO>()
                .eq(EnergyAppUserDO::getCardNo, cardNo));
    }

    default List<EnergyAppUserDO> selectSimpleList(String keyword) {
        return selectList(new LambdaQueryWrapperX<EnergyAppUserDO>()
                .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                        .like(EnergyAppUserDO::getUsername, keyword)
                        .or()
                        .like(EnergyAppUserDO::getNickname, keyword)
                        .or()
                        .like(EnergyAppUserDO::getMobile, keyword)
                        .or()
                        .like(EnergyAppUserDO::getCardNo, keyword))
                .orderByDesc(EnergyAppUserDO::getCreateTime));
    }

    default List<EnergyAppUserDO> selectEnabledSimpleList(String keyword) {
        return selectList(new LambdaQueryWrapperX<EnergyAppUserDO>()
                .eq(EnergyAppUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                        .like(EnergyAppUserDO::getUsername, keyword)
                        .or()
                        .like(EnergyAppUserDO::getNickname, keyword)
                        .or()
                        .like(EnergyAppUserDO::getMobile, keyword)
                        .or()
                        .like(EnergyAppUserDO::getCardNo, keyword))
                .orderByDesc(EnergyAppUserDO::getCreateTime));
    }

}
