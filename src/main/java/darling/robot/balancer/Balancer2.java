package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.Portfolio;
import darling.shared.Utils;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;
import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT_TRIGGER;
import static darling.shared.ApplicationProperties.PERCENT_PROFIT_LAG;
import static darling.shared.Constants.HUNDRED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL;
import static ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT;
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
            closeProfitDeal(deal, lastPrice);
        });
        postOrder(instrumentDeals);
        portfolio.updateDealsWithCalculatedData(instrumentDeals);
        marketContext.savePortfolio(portfolio);
    }

    private void postOrder(List<Deal> deals) {
        if (deals.isEmpty()) {
            marketContext.postOrder(INSTRUMENT_UID, 1L, ZERO, ORDER_DIRECTION_BUY, Utils.buyAccountId(), ORDER_TYPE_MARKET);
        }
    }

    private void setTakeProfit(Deal deal, BigDecimal lastPrice) {
        BigDecimal standardMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal currentPercentDelta = lastPrice
                .subtract(deal.getPrice())
                .divide(deal.getPrice(), 9, HALF_UP)
                .multiply(HUNDRED);
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        currentPercentDelta = isSell ? currentPercentDelta.negate() : currentPercentDelta;
        int deltaCountInCurrentPrice = currentPercentDelta.divide(PERCENT_DELTA_PROFIT, 0, DOWN).intValue();

        BigDecimal newTakeProfitPrice = ZERO;
        BigDecimal dealPrice = deal.getPrice();
        BigDecimal triggerMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT_TRIGGER).divide(HUNDRED, 9, HALF_UP);
        for (int i = 1; i < deltaCountInCurrentPrice; i++) {
            BigDecimal testMoneyDelta = standardMoneyDelta
                    .multiply(BigDecimal.valueOf(i))
                    .add(triggerMoneyDelta);
            BigDecimal testTakeProfitPrice = isSell ? dealPrice.subtract(testMoneyDelta) : dealPrice.add(testMoneyDelta);
            if (testTakeProfitPrice.compareTo(lastPrice) > 0) break;
            newTakeProfitPrice = isSell ? testTakeProfitPrice.add(triggerMoneyDelta) : testTakeProfitPrice.subtract(triggerMoneyDelta);
        }
        newTakeProfitPrice = isSell ? min(newTakeProfitPrice, deal.getTakeProfitPrice()) : max(newTakeProfitPrice, deal.getTakeProfitPrice());
        deal.setTakeProfitPrice(newTakeProfitPrice);
    }

    private void closeProfitDeal(Deal deal, BigDecimal lastPrice) {
        // Продажа/покупка, ГЭП
        if (lastPrice.compareTo(deal.getTakeProfitPrice()) > 0) {
            return;
        }
        BigDecimal standardMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal moneyLag = standardMoneyDelta.multiply(PERCENT_PROFIT_LAG).divide(HUNDRED, 9, HALF_UP);
        BigDecimal minTakeProfit = deal.getPrice().add(standardMoneyDelta).subtract(moneyLag);
        if (lastPrice.compareTo(minTakeProfit) < 0) {
            deal.setTakeProfitPrice(ZERO);
            return;
        }
        OrderDirection direction = deal.getType().equals(OPERATION_TYPE_SELL) ? ORDER_DIRECTION_BUY : ORDER_DIRECTION_SELL;
        String accountId = deal.getType().equals(OPERATION_TYPE_SELL) ? Utils.buyAccountId() : Utils.sellAccountId();
        marketContext.postOrder(INSTRUMENT_UID, 1L, lastPrice, direction, accountId, ORDER_TYPE_LIMIT);
    }
}