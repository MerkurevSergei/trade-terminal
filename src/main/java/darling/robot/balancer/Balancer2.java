package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.Portfolio;
import darling.shared.Utils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Comparators.max;
import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT_TRIGGER;
import static darling.shared.Constants.HUNDRED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;
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
        Optional<LastPrice> optLastPrices = marketContext.getLastPrice(INSTRUMENT_UID);
        if (optLastPrices.isEmpty()) return;
        BigDecimal lastPrice = optLastPrices.get().price();
        Portfolio portfolio = marketContext.getPortfolio();
        List<Deal> instrumentDeals = marketContext.getPortfolio().getOpenDeals(INSTRUMENT_UID);

        instrumentDeals.forEach(deal -> {
            setTakeProfit(deal, lastPrice);
//            shiftStop(deal, lastPrice);
//            closeDeal(deal, lastPrice);
        });
        portfolio.updateDealsWithCalculatedData(instrumentDeals);
        marketContext.savePortfolio(portfolio);
        //postOrder(instrumentDeals);
    }

    private void postOrder(List<Deal> deals) {
        if (deals.isEmpty()) {
            marketContext.postOrder(INSTRUMENT_UID, 1L, ZERO, ORDER_DIRECTION_BUY, Utils.buyAccountId(), ORDER_TYPE_MARKET);
        }
    }

    private void setTakeProfit(Deal deal, BigDecimal lastPrice) {
        BigDecimal currentPercentDelta = lastPrice
                .subtract(deal.getPrice())
                .divide(deal.getPrice(), 9, HALF_UP)
                .multiply(HUNDRED);
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        currentPercentDelta = isSell ? currentPercentDelta.negate() : currentPercentDelta;
        BigDecimal moneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal newTakeProfitPrice = currentPercentDelta.divide(PERCENT_DELTA_PROFIT, 0, DOWN).multiply(moneyDelta);
        newTakeProfitPrice = max(newTakeProfitPrice, deal.getTakeProfitPrice());

        if (currentPercentDelta.compareTo(PERCENT_DELTA_PROFIT_TRIGGER) < 0) return;

        BigDecimal takeProfitPrice = isSell ? deal.getPrice().subtract(newTakeProfitPrice) : deal.getPrice().add(newTakeProfitPrice);
        deal.setTakeProfitPrice(takeProfitPrice);
    }
}