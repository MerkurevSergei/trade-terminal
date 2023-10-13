package darling.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TinkoffSpecialTypeMapper {

    static BigDecimal map(Quotation quotation) {
        return quotation.getUnits() == 0 && quotation.getNano() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), 9));
    }

    static BigDecimal map(MoneyValue moneyValue) {
        return moneyValue.getUnits() == 0 && moneyValue.getNano() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(moneyValue.getUnits()).add(BigDecimal.valueOf(moneyValue.getNano(), 9));
    }

    static Instant map(LocalDateTime dateTime) {
        return Instant.ofEpochSecond(dateTime.toEpochSecond(ZoneOffset.UTC));
    }

    static LocalDateTime map(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    static InstrumentType map(String instrumentType) {
        return InstrumentType.valueOf("INSTRUMENT_TYPE_" + instrumentType.toUpperCase());
    }
}