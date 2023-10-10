package stocks.domain.balancer;

import stocks.domain.model.Bet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatRecord(Bet bet, LocalDateTime closed, BigDecimal profitPercent) {
}
