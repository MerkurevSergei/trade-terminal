package darling.mapper;

import darling.domain.Operation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.tinkoff.piapi.contract.v1.OperationItem;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OperationMapper {

    OperationMapper INST = Mappers.getMapper(OperationMapper.class);

    @Mapping(target = "instrumentType", source = "instrumentKind")
    Operation map(OperationItem item);
}