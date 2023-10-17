package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.Portfolio;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static darling.context.event.Event.CONTEXT_STARTED;

@RequiredArgsConstructor
public class Balancer2 implements EventListener {

    private final MarketContext marketContext;

    private static final String INSTRUMENT_UID = "8e2b0325-0292-4654-8a18-4f63ed3b0e09";

    @Override
    public void handle(Event event) {
        if (!CONTEXT_STARTED.equals(event) || !marketContext.getActiveOrders(INSTRUMENT_UID).isEmpty()) {
            return;
        }
        Portfolio portfolio = marketContext.getPortfolio();
        List<Deal> portfolioDealsByUid = portfolio.getOpenDeals(INSTRUMENT_UID);
        makeFirstIsNeed(portfolioDealsByUid);

    }

    private void makeFirstIsNeed(List<Deal> portfolioDeals) {
        if (portfolioDeals.isEmpty()) {
            marketContext.postOrder(INSTRUMENT_UID);
        }
    }
}
