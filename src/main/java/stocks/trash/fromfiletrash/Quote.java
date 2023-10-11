package stocks.trash.fromfiletrash;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Quote(LocalDate date, BigDecimal low, BigDecimal high, BigDecimal open, BigDecimal close,
                    BigDecimal volume) {
}