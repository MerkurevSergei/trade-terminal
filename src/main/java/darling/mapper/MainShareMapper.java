package darling.mapper;

import darling.domain.MainShare;
import darling.domain.Share;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MainShareMapper {

    MainShareMapper INST = Mappers.getMapper(MainShareMapper.class);

    MainShare map(Share share);

    List<MainShare> map(List<Share> share);
}