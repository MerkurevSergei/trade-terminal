package darling.domain;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.RealExchange;

import java.time.LocalDateTime;

@Builder
public record Share(String uid, String figi, String ticker, String name, Integer lot, String currency, String exchange,
                    RealExchange realExchange, Boolean shortEnabledFlag, String countryOfRisk, String sector,
                    String classCode, String shareType, LocalDateTime first1MinCandleDate,
                    LocalDateTime first1DayCandleDate) {
}