package darling.domain;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.InstrumentType;


/**
 * Позиция.
 */
@Builder
public record Position(String instrumentUid, Long blocked, Long balance, boolean exchangeBlocked,
                       InstrumentType instrumentType) {
}