package stock.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.math.BigDecimal.ZERO;

@Getter
@RequiredArgsConstructor
public final class Bet {
    private final LocalDateTime time;
    private final BigDecimal price;
    private final Direction direction;
    private BigDecimal stopLoss = ZERO;
    private BigDecimal takeProfit = ZERO;

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }

    public enum Direction {
        UP,
        DOWN
    }
}
