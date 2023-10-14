package darling.domain;

import lombok.Builder;
import lombok.Setter;
import lombok.ToString;
import ru.tinkoff.piapi.contract.v1.InstrumentType;

import java.util.Objects;


/**
 * Позиция.
 */
@Setter
@Builder
public final class Position {
    private String instrumentUid;
    private final String figi;
    private String ticker;
    private String name;
    private final Long balance;
    private Long lotBalance;
    private final Long blocked;

    public String figi() {
        return figi;
    }

    public Long balance() {
        return balance;
    }

    public String instrumentUid() {
        return instrumentUid;
    }

    public String ticker() {
        return ticker;
    }

    public String name() {
        return name;
    }

    public Long lotBalance() {
        return lotBalance;
    }

    public Long blocked() {
        return blocked;
    }


}
