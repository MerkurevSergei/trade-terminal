package darling.domain;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


/**
 * Операция.
 */
@Builder
public record Operation(String id, String brokerAccountId, String parentOperationId, String name, LocalDateTime date,
                        OperationType type, String description, OperationState state, String instrumentUid,
                        InstrumentType instrumentType, BigDecimal payment, BigDecimal price, BigDecimal commission,
                        long quantity, long quantityRest, long quantityDone) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return Objects.equals(id, operation.id) && Objects.equals(brokerAccountId, operation.brokerAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brokerAccountId);
    }
}