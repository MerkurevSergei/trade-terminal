package darling.mapper;

import darling.domain.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.tinkoff.piapi.core.models.SecurityPosition;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PositionMapper {

    PositionMapper INST = Mappers.getMapper(PositionMapper.class);

    @Mapping(target = "instrumentUid", source = "figi")
    Position map(SecurityPosition position);
}