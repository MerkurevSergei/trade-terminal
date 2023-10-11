package stocks.trash.fromfiletrash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoryCandle(String figi, LocalDateTime startTime, BigDecimal open, BigDecimal close, BigDecimal high,
                            BigDecimal low, BigDecimal volume) {
}
