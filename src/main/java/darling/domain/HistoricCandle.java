package darling.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricCandle(LocalDateTime time, BigDecimal open, BigDecimal close, BigDecimal low, BigDecimal high,
                             Long volume, boolean isComplete) {
}
