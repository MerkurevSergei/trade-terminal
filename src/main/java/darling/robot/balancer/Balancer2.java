package darling.robot.balancer;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.domain.Portfolio;
import darling.domain.order.Order;
import darling.shared.FinUtils;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

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
import static darling.shared.ApplicationProperties.EMPTY_LEVEL_LAG;
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
    private final MainShare mainShare;

    private LocalDateTime lastAction = LocalDateTime.now(ZoneOffset.UTC);
    private OrderDirection lastProfitOrderDirection = ORDER_DIRECTION_BUY;

    @Override
    public void handle(Event event) {
        if (!CONTEXT_REFRESHED.equals(event)) {
            return;
        }
        List<Order> activeOrders = marketContext.getActiveOrders(mainShare.uid());
        closeFrozenOrders(activeOrders);
        if (!activeOrders.isEmpty()) {
            return;
        }

        Optional<LastPrice> optLastPrices = marketContext.getLastPrice(mainShare.uid());
        if (optLastPrices.isEmpty()) return;
        BigDecimal lastPrice = optLastPrices.get().price();
        Portfolio portfolio = marketContext.getPortfolio();
        List<Deal> instrumentDeals = marketContext.getPortfolio().getOpenDeals(mainShare.uid());

        instrumentDeals.forEach(deal -> {
            setTakeProfit(deal, lastPrice);
            clearTakeProfit(deal, lastPrice);
            closeProfitDeal(deal, lastPrice);
        });
        postOrder(instrumentDeals, lastPrice);
        portfolio.updateDealsWithCalculatedData(instrumentDeals);
        marketContext.savePortfolio(portfolio);
    }

    // ====================== ФУНКЦИИ ОСНОВНОГО ЦИКЛА ====================== //

    private void setTakeProfit(Deal deal, BigDecimal lastPrice) {
        BigDecimal standardMoneyDelta = deal.getOpenPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal currentPercentDelta = FinUtils.getProfitPercent(deal.getOpenPrice(), lastPrice, deal.getType());
        int deltaCountInCurrentPrice = currentPercentDelta.divide(PERCENT_DELTA_PROFIT, 0, DOWN).intValue();
        BigDecimal triggerMoneyDelta = deal.getOpenPrice().multiply(PERCENT_DELTA_PROFIT_TRIGGER).divide(HUNDRED, 9, HALF_UP);

        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        BigDecimal newTakeProfitPrice = ZERO;
        BigDecimal dealPrice = deal.getOpenPrice();
        for (int i = 1; i <= deltaCountInCurrentPrice; i++) {
            BigDecimal testMoneyDelta = standardMoneyDelta
                    .multiply(BigDecimal.valueOf(i))
                    .add(triggerMoneyDelta);
            BigDecimal testTakeProfitPrice = isSell ? dealPrice.subtract(testMoneyDelta) : dealPrice.add(testMoneyDelta);
            if (!isSell && testTakeProfitPrice.compareTo(lastPrice) >= 0) break;
            if (isSell && testTakeProfitPrice.compareTo(lastPrice) <= 0) break;
            newTakeProfitPrice = isSell ? testTakeProfitPrice.add(triggerMoneyDelta) : testTakeProfitPrice.subtract(triggerMoneyDelta);
        }

        BigDecimal oldSellTakeProfitPrice = deal.getTakeProfitPrice().compareTo(ZERO) == 0 ? newTakeProfitPrice : deal.getTakeProfitPrice();
        newTakeProfitPrice = isSell ? min(newTakeProfitPrice, oldSellTakeProfitPrice) : max(newTakeProfitPrice, deal.getTakeProfitPrice());
        if (newTakeProfitPrice.compareTo(ZERO) == 0) return;
        deal.setTakeProfitPrice(newTakeProfitPrice);
    }

    private void clearTakeProfit(Deal deal, BigDecimal lastPrice) {
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean isBuy = deal.getType().equals(OPERATION_TYPE_BUY);
        BigDecimal standardMoneyDelta = deal.getOpenPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal lagMoney = standardMoneyDelta.multiply(PERCENT_PROFIT_LAG).divide(HUNDRED, 9, HALF_UP);
        BigDecimal standardMoneyDeltaWithLag = standardMoneyDelta.subtract(lagMoney);
        BigDecimal dealPriceWithMinTakeProfit = isSell ? deal.getOpenPrice().subtract(standardMoneyDeltaWithLag) : deal.getOpenPrice().add(standardMoneyDeltaWithLag);
        int compareProfit = lastPrice.compareTo(dealPriceWithMinTakeProfit);
        if ((isBuy && compareProfit < 0) || (isSell && compareProfit > 0)) {
            deal.setTakeProfitPrice(ZERO);
        }
    }

    private void closeProfitDeal(Deal deal, BigDecimal lastPrice) {
        if (deal.getTakeProfitPrice().compareTo(ZERO) == 0) {
            return;
        }

        boolean isBuy = deal.getType().equals(OPERATION_TYPE_BUY);
        boolean takeProfitNotBrakeIsBuy = isBuy && lastPrice.compareTo(deal.getTakeProfitPrice()) > 0;
        if (takeProfitNotBrakeIsBuy) {
            return;
        }

        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean takeProfitNotBrakeIsSell = isSell && lastPrice.compareTo(deal.getTakeProfitPrice()) < 0;
        if (takeProfitNotBrakeIsSell) {
            return;
        }
        OrderDirection direction = deal.getType().equals(OPERATION_TYPE_SELL) ? ORDER_DIRECTION_BUY : ORDER_DIRECTION_SELL;
        lastProfitOrderDirection = direction;
        postOrderWithRepeatProtected(mainShare.uid(), deal.getQuantity() / mainShare.lot(), lastPrice, direction, deal.getAccountId(), ORDER_TYPE_LIMIT);
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

    private void postOrder(List<Deal> deals, BigDecimal lastPrice) {
        if (!isEmptyLevel(deals, lastPrice)) {
            return;
        }
        long unbalancedDeal = deals.stream()
                .filter(deal -> deal.getTakeProfitPrice().compareTo(ZERO) == 0)
                .map(deal -> deal.getType().equals(OPERATION_TYPE_SELL) ? -1 * deal.getQuantity() : deal.getQuantity())
                .mapToLong(value -> value)
                .sum();
        if (unbalancedDeal > 0) {
            postOrderWithRepeatProtected(mainShare.uid(), unbalancedDeal / mainShare.lot(), ZERO, ORDER_DIRECTION_SELL, ACCOUNT_SELL, ORDER_TYPE_MARKET);
        } else if (unbalancedDeal < 0) {
            postOrderWithRepeatProtected(mainShare.uid(), -1 * unbalancedDeal / mainShare.lot(), ZERO, ORDER_DIRECTION_BUY, ACCOUNT_BUY, ORDER_TYPE_MARKET);
        } else {
            String accountId = lastProfitOrderDirection.equals(ORDER_DIRECTION_SELL) ? ACCOUNT_SELL : ACCOUNT_BUY;
            postOrderWithRepeatProtected(mainShare.uid(), 1L, ZERO, lastProfitOrderDirection, accountId, ORDER_TYPE_MARKET);
        }
    }

    private boolean isEmptyLevel(List<Deal> deals, BigDecimal lastPrice) {
        BigDecimal standardMoneyDelta = lastPrice.multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal lagMoney = standardMoneyDelta.multiply(EMPTY_LEVEL_LAG).divide(HUNDRED, 9, HALF_UP);
        BigDecimal boundUp = lastPrice.add(standardMoneyDelta).subtract(lagMoney);
        BigDecimal boundDown = lastPrice.subtract(standardMoneyDelta).subtract(lagMoney);
        OperationType lastProfitType = lastProfitOrderDirection.equals(ORDER_DIRECTION_SELL) ? OPERATION_TYPE_SELL : OPERATION_TYPE_BUY;
        List<Deal> directedDeals = deals.stream()
                .filter(deal -> deal.getType().equals(lastProfitType))
                .toList();
        for (Deal deal : directedDeals) {
            if (boundUp.compareTo(deal.getOpenPrice()) > 0 && boundDown.compareTo(deal.getOpenPrice()) < 0) {
                return false;
            }
        }
        return true;
    }

    private void postOrderWithRepeatProtected(String instrumentId, long lot, BigDecimal price, OrderDirection direction,
                                              String accountId, OrderType type) {
        if (ChronoUnit.SECONDS.between(lastAction, LocalDateTime.now(ZoneOffset.UTC)) < 15) {
            return;
        }
        marketContext.postOrder(instrumentId, lot, price, direction, accountId, type);
        lastAction = LocalDateTime.now(ZoneOffset.UTC);
    }
}