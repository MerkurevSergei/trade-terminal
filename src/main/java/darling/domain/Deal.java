package darling.domain;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Deal {
    private final Operation openOperation;
    private Operation closeOperation;
    private long quantity;

    public Deal(Operation openOperation, Long quantity) {
        this.openOperation = openOperation;
        this.quantity = quantity;
    }

    public String getInstrumentUid() {
        return openOperation.instrumentUid();
    }

    public String getAccountId() {
        return openOperation.brokerAccountId();
    }

    public LocalDateTime getDate() {
        return openOperation.date();
    }

    public OperationType getType() {
        return openOperation.type();
    }

    public String getOpenOperationId() {
        return openOperation.id();
    }

    public boolean isClosed() {
        return closeOperation != null;
    }

    public BigDecimal getPayment() {
        return openOperation.payment();
    }

    public BigDecimal getPrice() {
        return openOperation.price();
    }

    public long getQuantity() {
        return quantity;
    }


    public void setCloseOperation(Operation operation) {
        this.closeOperation = operation;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
