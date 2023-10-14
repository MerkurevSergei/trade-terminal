package darling.mapper;

import darling.domain.Share;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = TinkoffSpecialTypeMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShareMapper {

    ShareMapper INST = Mappers.getMapper(ShareMapper.class);

    Share map(ru.tinkoff.piapi.contract.v1.Share share);

    List<Share> map(List<ru.tinkoff.piapi.contract.v1.Share> share);

    @Mapping(target = "weekendFlag", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "uidBytes", ignore = true)
    @Mapping(target = "tradingStatusValue", ignore = true)
    @Mapping(target = "tradingStatus", ignore = true)
    @Mapping(target = "tickerBytes", ignore = true)
    @Mapping(target = "shareTypeValue", ignore = true)
    @Mapping(target = "sellAvailableFlag", ignore = true)
    @Mapping(target = "sectorBytes", ignore = true)
    @Mapping(target = "realExchangeValue", ignore = true)
    @Mapping(target = "positionUidBytes", ignore = true)
    @Mapping(target = "positionUid", ignore = true)
    @Mapping(target = "otcFlag", ignore = true)
    @Mapping(target = "nominal", ignore = true)
    @Mapping(target = "nameBytes", ignore = true)
    @Mapping(target = "minPriceIncrement", ignore = true)
    @Mapping(target = "mergeUnknownFields", ignore = true)
    @Mapping(target = "mergeNominal", ignore = true)
    @Mapping(target = "mergeMinPriceIncrement", ignore = true)
    @Mapping(target = "mergeKshort", ignore = true)
    @Mapping(target = "mergeKlong", ignore = true)
    @Mapping(target = "mergeIpoDate", ignore = true)
    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "mergeFirst1MinCandleDate", ignore = true)
    @Mapping(target = "mergeFirst1DayCandleDate", ignore = true)
    @Mapping(target = "mergeDshortMin", ignore = true)
    @Mapping(target = "mergeDshort", ignore = true)
    @Mapping(target = "mergeDlongMin", ignore = true)
    @Mapping(target = "mergeDlong", ignore = true)
    @Mapping(target = "liquidityFlag", ignore = true)
    @Mapping(target = "kshort", ignore = true)
    @Mapping(target = "klong", ignore = true)
    @Mapping(target = "issueSizePlan", ignore = true)
    @Mapping(target = "issueSize", ignore = true)
    @Mapping(target = "isinBytes", ignore = true)
    @Mapping(target = "isin", ignore = true)
    @Mapping(target = "ipoDate", ignore = true)
    @Mapping(target = "forQualInvestorFlag", ignore = true)
    @Mapping(target = "forIisFlag", ignore = true)
    @Mapping(target = "first1MinCandleDate", ignore = true)
    @Mapping(target = "first1DayCandleDate", ignore = true)
    @Mapping(target = "figiBytes", ignore = true)
    @Mapping(target = "exchangeBytes", ignore = true)
    @Mapping(target = "dshortMin", ignore = true)
    @Mapping(target = "dshort", ignore = true)
    @Mapping(target = "dlongMin", ignore = true)
    @Mapping(target = "dlong", ignore = true)
    @Mapping(target = "divYieldFlag", ignore = true)
    @Mapping(target = "currencyBytes", ignore = true)
    @Mapping(target = "countryOfRiskNameBytes", ignore = true)
    @Mapping(target = "countryOfRiskName", ignore = true)
    @Mapping(target = "countryOfRiskBytes", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "classCodeBytes", ignore = true)
    @Mapping(target = "buyAvailableFlag", ignore = true)
    @Mapping(target = "blockedTcaFlag", ignore = true)
    @Mapping(target = "apiTradeAvailableFlag", ignore = true)
    ru.tinkoff.piapi.contract.v1.Share map(Share share);
}