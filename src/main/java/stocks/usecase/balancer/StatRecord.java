package stocks.usecase.balancer;

import stocks.domain.Bet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatRecord(Bet bet, LocalDateTime closed, BigDecimal profitPercent) {
}
