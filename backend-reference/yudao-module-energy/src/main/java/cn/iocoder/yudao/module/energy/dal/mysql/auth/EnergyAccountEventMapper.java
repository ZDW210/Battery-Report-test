package cn.iocoder.yudao.module.energy.dal.mysql.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.energy.controller.admin.auth.vo.EnergyAccountEventPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.auth.EnergyAccountEventDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnergyAccountEventMapper extends BaseMapperX<EnergyAccountEventDO> {

    default PageResult<EnergyAccountEventDO> selectPage(EnergyAccountEventPageReqVO reqVO) {
        LambdaQueryWrapperX<EnergyAccountEventDO> wrapper = new LambdaQueryWrapperX<EnergyAccountEventDO>()
                .eqIfPresent(EnergyAccountEventDO::getEventScene, reqVO.getEventScene())
                .eqIfPresent(EnergyAccountEventDO::getAuthType, reqVO.getAuthType())
                .eqIfPresent(EnergyAccountEventDO::getAccountKnown, reqVO.getAccountKnown())
                .betweenIfPresent(EnergyAccountEventDO::getCreateTime, reqVO.getCreateTime());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(query -> query
                    .like(EnergyAccountEventDO::getAccountName, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getAccountMobile, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getCardNo, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getDeviceNo, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getDeviceName, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getMeterNo, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getGatewaySn, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getMeterSn, reqVO.getKeyword())
                    .or()
                    .like(EnergyAccountEventDO::getScanText, reqVO.getKeyword()));
        }
        return selectPage(reqVO, wrapper
                .orderByDesc(EnergyAccountEventDO::getCreateTime)
                .orderByDesc(EnergyAccountEventDO::getId));
    }

}
