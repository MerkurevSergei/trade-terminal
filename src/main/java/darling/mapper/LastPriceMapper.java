package darling.mapper;

import darling.domain.LastPrice;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LastPriceMapper {

    LastPriceMapper INST = Mappers.getMapper(LastPriceMapper.class);

    LastPrice map(ru.tinkoff.piapi.contract.v1.LastPrice lastPrice);
}
