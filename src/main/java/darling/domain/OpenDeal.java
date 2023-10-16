package darling.domain;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OpenDeal {
    private final Operation operation;
    private long quantity;

    public OpenDeal(Operation operation) {
        this.operation = operation;
        this.quantity = operation.quantityDone();
    }

    public String getInstrumentUid() {
        return operation.instrumentUid();
    }

    public String getAccountId() {
        return operation.brokerAccountId();
    }

    public LocalDateTime getDate() {
        return operation.date();
    }

    public OperationType getType() {
        return operation.type();
    }

    public BigDecimal getPayment() {
        return operation.payment();
    }

    public BigDecimal getPrice() {
        return operation.price();
    }

    public long getQuantity() {
        return quantity;
    }


    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
