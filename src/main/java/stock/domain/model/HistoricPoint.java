package stock.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricPoint(LocalDateTime time, BigDecimal price, Long volume) {
    public Point point() {
        return new Point(time, price);
    }
}
