package stocks.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Point(LocalDateTime time, BigDecimal price) {

}
