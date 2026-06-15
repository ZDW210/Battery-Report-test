package cn.iocoder.yudao.module.energy.service.telemetry;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatReqVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryDailyStatRespVO;
import cn.iocoder.yudao.module.energy.controller.admin.telemetry.vo.EnergyTelemetryPageReqVO;
import cn.iocoder.yudao.module.energy.dal.dataobject.telemetry.EnergyTelemetryDO;
import cn.iocoder.yudao.module.energy.dal.mysql.telemetry.EnergyTelemetryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Validated
public class EnergyTelemetryServiceImpl implements EnergyTelemetryService {

    @Resource
    private EnergyTelemetryMapper telemetryMapper;

    @Override
    public PageResult<EnergyTelemetryDO> getTelemetryPage(EnergyTelemetryPageReqVO pageReqVO) {
        return telemetryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EnergyTelemetryDO> getTelemetryList(EnergyTelemetryPageReqVO pageReqVO, Integer limit) {
        return telemetryMapper.selectList(pageReqVO, limit);
    }

    @Override
    public List<EnergyTelemetryDailyStatRespVO> getDailyStatList(EnergyTelemetryDailyStatReqVO reqVO) {
        EnergyTelemetryPageReqVO pageReqVO = new EnergyTelemetryPageReqVO();
        pageReqVO.setDeviceId(reqVO.getDeviceId());
        pageReqVO.setGatewaySn(reqVO.getGatewaySn());
        pageReqVO.setMeterNo(reqVO.getMeterNo());
        pageReqVO.setCollectTime(reqVO.getCollectTime());

        Function<EnergyTelemetryDO, BigDecimal> metricGetter = getMetricGetter(reqVO.getMetric());
        Map<LocalDate, List<EnergyTelemetryDO>> dayValueMap = telemetryMapper.selectList(pageReqVO, 5000).stream()
                .filter(item -> item.getCollectTime() != null)
                .filter(item -> metricGetter.apply(item) != null)
                .collect(Collectors.groupingBy(item -> item.getCollectTime().toLocalDate(), TreeMap::new,
                        Collectors.toList()));

        return dayValueMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> buildDailyStat(entry.getKey(), entry.getValue(), metricGetter))
                .collect(Collectors.toList());
    }

    private EnergyTelemetryDailyStatRespVO buildDailyStat(LocalDate date, List<EnergyTelemetryDO> records,
                                                          Function<EnergyTelemetryDO, BigDecimal> metricGetter) {
        List<BigDecimal> values = records.stream().map(metricGetter).filter(Objects::nonNull).toList();
        EnergyTelemetryDO maxRecord = records.stream()
                .max((left, right) -> metricGetter.apply(left).compareTo(metricGetter.apply(right)))
                .orElse(null);
        EnergyTelemetryDO minRecord = records.stream()
                .min((left, right) -> metricGetter.apply(left).compareTo(metricGetter.apply(right)))
                .orElse(null);
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
        return EnergyTelemetryDailyStatRespVO.builder()
                .date(date)
                .max(maxRecord != null ? metricGetter.apply(maxRecord) : null)
                .maxTime(maxRecord != null ? maxRecord.getCollectTime() : null)
                .min(minRecord != null ? metricGetter.apply(minRecord) : null)
                .minTime(minRecord != null ? minRecord.getCollectTime() : null)
                .avg(avg)
                .build();
    }

    private Function<EnergyTelemetryDO, BigDecimal> getMetricGetter(String metric) {
        return switch (metric) {
            case "pa" -> EnergyTelemetryDO::getPa;
            case "pb" -> EnergyTelemetryDO::getPb;
            case "pc" -> EnergyTelemetryDO::getPc;
            case "p" -> EnergyTelemetryDO::getP;
            case "ua" -> EnergyTelemetryDO::getUa;
            case "ub" -> EnergyTelemetryDO::getUb;
            case "uc" -> EnergyTelemetryDO::getUc;
            case "ia" -> EnergyTelemetryDO::getIa;
            case "ib" -> EnergyTelemetryDO::getIb;
            case "ic" -> EnergyTelemetryDO::getIc;
            case "pf" -> EnergyTelemetryDO::getPf;
            case "epi" -> EnergyTelemetryDO::getEpi;
            default -> item -> null;
        };
    }

}
