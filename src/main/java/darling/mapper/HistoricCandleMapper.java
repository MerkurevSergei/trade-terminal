package darling.mapper;

import darling.domain.HistoricCandle;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HistoricCandleMapper {

    HistoricCandleMapper INST = Mappers.getMapper(HistoricCandleMapper.class);

    HistoricCandle map(ru.tinkoff.piapi.contract.v1.HistoricCandle candle);

    List<HistoricCandle> map(List<ru.tinkoff.piapi.contract.v1.HistoricCandle> candles);
}