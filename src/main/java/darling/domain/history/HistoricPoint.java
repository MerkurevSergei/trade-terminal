package darling.domain.history;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricPoint(LocalDateTime time, BigDecimal price, Long volume) {
}