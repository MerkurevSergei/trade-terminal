package darling.domain;

import darling.domain.order.OrderDirection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.math.BigDecimal.ZERO;

@Getter
@ToString
@RequiredArgsConstructor
public final class Bet {
    private final LocalDateTime time;
    private final BigDecimal price;
    private final OrderDirection direction;
    private BigDecimal stopLoss = ZERO;
    private BigDecimal takeProfit = ZERO;

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }
}
