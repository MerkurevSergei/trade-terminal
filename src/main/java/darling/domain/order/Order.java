package darling.domain.order;

import darling.domain.Share;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;

import java.time.LocalDateTime;

public record Order(String orderId, LocalDateTime date, Share share, OrderExecutionReportStatus status, long lotsRequested, long lotsExecuted) {

    public long lotsRest() {
        return lotsRequested - lotsExecuted;
    }
}
