package darling.domain.positions.model;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.tinkoff.piapi.contract.v1.OperationItem;
import darling.shared.TinkoffTypeMapper;

@Mapper(uses = TinkoffTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OperationMapper {

    OperationMapper INST = Mappers.getMapper(OperationMapper.class);

    Operation map(OperationItem item);
}