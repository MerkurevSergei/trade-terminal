package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.Portfolio;
import darling.domain.order.Order;
import darling.shared.FinUtils;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;
import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.shared.ApplicationProperties.ACCOUNT_BUY;
import static darling.shared.ApplicationProperties.ACCOUNT_SELL;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT_TRIGGER;
import static darling.shared.ApplicationProperties.PERCENT_PROFIT_LAG;
import static darling.shared.ApplicationProperties.TIME_TO_FROZEN_SECOND;
import static darling.shared.Constants.HUNDRED;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL;
import static ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT;
import static ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_MARKET;

@RequiredArgsConstructor
public class Balancer2 implements EventListener {

    private final MarketContext marketContext;

    private static final String INSTRUMENT_UID = "8e2b0325-0292-4654-8a18-4f63ed3b0e09";
    private static final Long LOT_COUNT = 10000L;

    @Override
    public void handle(Event event) {
        if (!CONTEXT_REFRESHED.equals(event)) {
            return;
        }
        List<Order> activeOrders = marketContext.getActiveOrders(INSTRUMENT_UID);
        closeFrozenOrders(activeOrders);
        if (!activeOrders.isEmpty()) {
            return;
        }

        Optional<LastPrice> optLastPrices = marketContext.getLastPrice(INSTRUMENT_UID);
        if (optLastPrices.isEmpty()) return;
        BigDecimal lastPrice = optLastPrices.get().price();
        Portfolio portfolio = marketContext.getPortfolio();
        List<Deal> instrumentDeals = marketContext.getPortfolio().getOpenDeals(INSTRUMENT_UID);

        instrumentDeals.forEach(deal -> {
            setTakeProfit(deal, lastPrice);
            clearTakeProfit(deal, lastPrice);
            closeProfitDeal(deal, lastPrice);
        });
        postOrder(instrumentDeals);
        portfolio.updateDealsWithCalculatedData(instrumentDeals);
        marketContext.savePortfolio(portfolio);
    }

    // ====================== ФУНКЦИИ ОСНОВНОГО ЦИКЛА ====================== //

    private void setTakeProfit(Deal deal, BigDecimal lastPrice) {
        BigDecimal standardMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal currentPercentDelta = FinUtils.getProfitPercent(deal.getPrice(), lastPrice, OPERATION_TYPE_SELL);
        int deltaCountInCurrentPrice = currentPercentDelta.divide(PERCENT_DELTA_PROFIT, 0, DOWN).intValue();
        BigDecimal triggerMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT_TRIGGER).divide(HUNDRED, 9, HALF_UP);

        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        BigDecimal newTakeProfitPrice = ZERO;
        BigDecimal dealPrice = deal.getPrice();
        for (int i = 1; i <= deltaCountInCurrentPrice; i++) {
            BigDecimal testMoneyDelta = standardMoneyDelta
                    .multiply(BigDecimal.valueOf(i))
                    .add(triggerMoneyDelta);
            BigDecimal testTakeProfitPrice = isSell ? dealPrice.subtract(testMoneyDelta) : dealPrice.add(testMoneyDelta);
            if (!isSell && testTakeProfitPrice.compareTo(lastPrice) > 0) break;
            if (isSell && testTakeProfitPrice.compareTo(lastPrice) < 0) break;
            newTakeProfitPrice = isSell ? testTakeProfitPrice.add(triggerMoneyDelta) : testTakeProfitPrice.subtract(triggerMoneyDelta);
        }

        BigDecimal oldSellTakeProfitPrice = deal.getTakeProfitPrice().compareTo(ZERO) == 0 ? newTakeProfitPrice : deal.getTakeProfitPrice();
        newTakeProfitPrice = isSell ? min(newTakeProfitPrice, oldSellTakeProfitPrice) : max(newTakeProfitPrice, deal.getTakeProfitPrice());
        deal.setTakeProfitPrice(newTakeProfitPrice);
    }

    private void clearTakeProfit(Deal deal, BigDecimal lastPrice) {
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean isBuy = deal.getType().equals(OPERATION_TYPE_BUY);
        BigDecimal standardMoneyDelta = deal.getPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal lagMoney = standardMoneyDelta.multiply(PERCENT_PROFIT_LAG).divide(HUNDRED, 9, HALF_UP);
        BigDecimal standardMoneyDeltaWithLag = standardMoneyDelta.subtract(lagMoney);
        BigDecimal dealPriceWithMinTakeProfit = isSell ? deal.getPrice().subtract(standardMoneyDeltaWithLag) : deal.getPrice().add(standardMoneyDeltaWithLag);
        int compareProfit = lastPrice.compareTo(dealPriceWithMinTakeProfit);
        if ((isBuy && compareProfit < 0) || (isSell && compareProfit > 0)) {
            deal.setTakeProfitPrice(ZERO);
        }
    }

    private void closeProfitDeal(Deal deal, BigDecimal lastPrice) {
        if (deal.getTakeProfitPrice().compareTo(ZERO) == 0) {
            return;
        }
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean takeProfitNotBrake = lastPrice.compareTo(deal.getTakeProfitPrice()) >= 0;
        takeProfitNotBrake = isSell != takeProfitNotBrake;
        if (takeProfitNotBrake) {
            return;
        }
        OrderDirection direction = deal.getType().equals(OPERATION_TYPE_SELL) ? ORDER_DIRECTION_BUY : ORDER_DIRECTION_SELL;
        marketContext.postOrder(INSTRUMENT_UID, deal.getQuantity() / LOT_COUNT, lastPrice, direction, deal.getAccountId(), ORDER_TYPE_LIMIT);
    }

    private void closeFrozenOrders(List<Order> activeOrders) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        for (Order order : activeOrders) {
            long diff = ChronoUnit.SECONDS.between(order.date(), now);
            boolean isFrozen = diff > TIME_TO_FROZEN_SECOND;
            if (isFrozen) {
                marketContext.cancelOrder(order.orderId(), order.accountId());
            }

        }
    }

    private void postOrder(List<Deal> deals) {
        if (deals.isEmpty()) {
            marketContext.postOrder(INSTRUMENT_UID, 1L, ZERO, ORDER_DIRECTION_BUY, ACCOUNT_BUY, ORDER_TYPE_MARKET);
        }
        long unbalancedDeal = deals.stream()
                .filter(deal -> deal.getTakeProfitPrice().compareTo(ZERO) == 0)
                .map(deal -> deal.getType().equals(OPERATION_TYPE_SELL) ? -1 * deal.getQuantity() : deal.getQuantity())
                .mapToLong(value -> value)
                .sum();
        if (unbalancedDeal > 0) {
            marketContext.postOrder(INSTRUMENT_UID, unbalancedDeal / LOT_COUNT, ZERO, ORDER_DIRECTION_SELL, ACCOUNT_SELL, ORDER_TYPE_MARKET);
        } else if (unbalancedDeal < 0) {
            marketContext.postOrder(INSTRUMENT_UID, -1 * unbalancedDeal / LOT_COUNT, ZERO, ORDER_DIRECTION_BUY, ACCOUNT_BUY, ORDER_TYPE_MARKET);
        }
    }
}