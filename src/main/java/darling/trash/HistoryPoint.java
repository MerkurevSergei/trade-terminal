package darling.trash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoryPoint(String figi, LocalDateTime startTime, BigDecimal price, BigDecimal volume) {
}
