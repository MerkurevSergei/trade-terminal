package darling.trash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointTtx(LocalDateTime tikTime, BigDecimal price, Integer dist020, Integer dist025, Integer dist030,
                       Integer dist050, Integer dist075, Integer dist100, Integer dist200) {

}
