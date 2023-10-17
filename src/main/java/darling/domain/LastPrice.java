package darling.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LastPrice(String instrumentUid, BigDecimal price, LocalDateTime time) {
}
