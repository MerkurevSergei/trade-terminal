package darling.domain.operations.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * Операция.
 */
@Builder
public record Operation(String id, String brokerAccountId, String parentOperationId, String name, LocalDateTime date, OperationType type,
                        String description, OperationState state, String instrumentUid, InstrumentType instrumentType,
                        BigDecimal payment, BigDecimal price, BigDecimal commission,
                        long quantity, long quantityRest, long quantityDone) {
}