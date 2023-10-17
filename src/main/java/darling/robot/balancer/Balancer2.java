package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.Portfolio;
import darling.shared.Utils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.context.event.Event.CONTEXT_STARTED;
import static java.math.BigDecimal.ZERO;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY;
import static ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_MARKET;

@RequiredArgsConstructor
public class Balancer2 implements EventListener {

    private final MarketContext marketContext;

    private static final String INSTRUMENT_UID = "8e2b0325-0292-4654-8a18-4f63ed3b0e09";

    @Override
    public void handle(Event event) {
        if (!CONTEXT_REFRESHED.equals(event) || !marketContext.getActiveOrders(INSTRUMENT_UID).isEmpty()) {
            return;
        }
        Portfolio portfolio = marketContext.getPortfolio();
        List<Deal> portfolioDealsByUid = portfolio.getOpenDeals(INSTRUMENT_UID);
        makeFirstIsNeed(portfolioDealsByUid);

    }

    private void makeFirstIsNeed(List<Deal> portfolioDeals) {
        if (portfolioDeals.isEmpty()) {
            marketContext.postOrder(INSTRUMENT_UID, 1L, ZERO, ORDER_DIRECTION_BUY, Utils.buyAccountId(), ORDER_TYPE_MARKET);
        }
    }
}
