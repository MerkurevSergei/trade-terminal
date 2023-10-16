package darling.domain;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Contract {
    private final String instrumentUid;
    private final String ticker;
    private final String brokerAccountId;
    private final LocalDateTime date;
    private final OperationType type;
    private final BigDecimal price;
    private final BigDecimal payment;
    private long quantity;

    public Contract(String instrumentUid, String ticker, String brokerAccountId, LocalDateTime date, OperationType type,
                    BigDecimal price, BigDecimal payment, long quantity) {
        this.instrumentUid = instrumentUid;
        this.ticker = ticker;
        this.brokerAccountId = brokerAccountId;
        this.date = date;
        this.type = type;
        this.price = price;
        this.payment = payment;
        this.quantity = quantity;
    }

    public Contract(Operation o) {
        this(o.instrumentUid(), o.name(), o.brokerAccountId(), o.date(), o.type(), o.price(), o.payment(), o.quantityDone());
    }

    public String instrumentUid() {
        return instrumentUid;
    }

    public String ticker() {
        return ticker;
    }

    public String brokerAccountId() {
        return brokerAccountId;
    }

    public LocalDateTime date() {
        return date;
    }

    public OperationType type() {
        return type;
    }

    public BigDecimal price() {
        return price;
    }

    public BigDecimal payment() {
        return payment;
    }

    public long quantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
