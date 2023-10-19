package darling.domain;

import lombok.EqualsAndHashCode;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Deal {

    @EqualsAndHashCode.Include
    private final Operation openOperation;

    @EqualsAndHashCode.Include
    private Operation closeOperation;

    private long quantity;

    private BigDecimal takeProfitPrice;

    public Deal(Operation openOperation, Long quantity, BigDecimal takeProfitPrice) {
        this.openOperation = openOperation;
        this.quantity = quantity;
        this.takeProfitPrice = takeProfitPrice;
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

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setCloseOperation(Operation operation) {
        this.closeOperation = operation;
    }



    public BigDecimal getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public void setTakeProfitPrice(BigDecimal takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }
}
