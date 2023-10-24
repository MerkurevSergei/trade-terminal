package darling.robot;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.domain.Portfolio;
import darling.domain.order.Order;
import darling.mapper.DirectionMapper;
import darling.shared.FinUtils;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;
import static darling.context.event.Event.CLOSE_DAY;
import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.shared.ApplicationProperties.ACCOUNT_BUY;
import static darling.shared.ApplicationProperties.ACCOUNT_SELL;
import static darling.shared.ApplicationProperties.EMPTY_LEVEL_DELTA;
import static darling.shared.ApplicationProperties.NEW_REQUEST_FROZEN_SECONDS;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT;
import static darling.shared.ApplicationProperties.PERCENT_DELTA_PROFIT_TRIGGER;
import static darling.shared.ApplicationProperties.PERCENT_PROFIT_CLEAR_LAG;
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
    private final boolean enableDelay;

    private LocalDateTime lastAction = LocalDateTime.now(ZoneOffset.UTC);
    private OrderDirection lastProfitOrderDirection = ORDER_DIRECTION_BUY;
    private Portfolio portfolio;
    private LastPrice lastPrice;
    private BigDecimal lastPriceValue;

    @Override
    public void handle(Event event) {
        closeDayIfNeed(event);
        if (!Objects.equals(CONTEXT_REFRESHED, event)) {
            return;
        }
        List<Order> activeOrders = marketContext.getActiveOrders(mainShare.uid());
        closeFrozenOrders(activeOrders);
        if (!activeOrders.isEmpty()) {
            return;
        }
        Optional<LastPrice> optLastPrices = marketContext.getLastPrice(mainShare.uid());
        if (optLastPrices.isEmpty()) {
            return;
        }
        portfolio = marketContext.getPortfolio();
        lastPrice = optLastPrices.get();
        lastPriceValue = lastPrice.price();

        List<Deal> instrumentDeals = portfolio.getOpenDeals(mainShare.uid());
        instrumentDeals.forEach(deal -> {
            setTakeProfit(deal);
            clearTakeProfit(deal);
            closeProfitDeal(deal);
        });
        postNewOrder(instrumentDeals);
        portfolio.updateDealsWithCalculatedData(instrumentDeals);
        marketContext.savePortfolio(portfolio);
    }

    // ====================== ФУНКЦИИ ОСНОВНОГО ЦИКЛА ====================== //

    private void setTakeProfit(Deal deal) {
        BigDecimal standardMoneyDelta = deal.getOpenPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal currentPercentDelta = FinUtils.getProfitPercent(deal.getOpenPrice(), lastPriceValue, deal.getType());
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
            if (!isSell && testTakeProfitPrice.compareTo(lastPriceValue) >= 0) break;
            if (isSell && testTakeProfitPrice.compareTo(lastPriceValue) <= 0) break;
            newTakeProfitPrice = isSell ? testTakeProfitPrice.add(triggerMoneyDelta) : testTakeProfitPrice.subtract(triggerMoneyDelta);
        }

        BigDecimal oldSellTakeProfitPrice = deal.getTakeProfitPrice().compareTo(ZERO) == 0 ? newTakeProfitPrice : deal.getTakeProfitPrice();
        newTakeProfitPrice = isSell ? min(newTakeProfitPrice, oldSellTakeProfitPrice) : max(newTakeProfitPrice, deal.getTakeProfitPrice());
        if (newTakeProfitPrice.compareTo(ZERO) == 0) return;
        deal.setTakeProfitPrice(newTakeProfitPrice);
    }

    private void clearTakeProfit(Deal deal) {
        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean isBuy = deal.getType().equals(OPERATION_TYPE_BUY);
        BigDecimal standardMoneyDelta = deal.getOpenPrice().multiply(PERCENT_DELTA_PROFIT).divide(HUNDRED, 9, HALF_UP);
        BigDecimal lagMoney = standardMoneyDelta.multiply(PERCENT_PROFIT_CLEAR_LAG).divide(HUNDRED, 9, HALF_UP);
        BigDecimal standardMoneyDeltaWithLag = standardMoneyDelta.subtract(lagMoney);
        BigDecimal dealPriceWithMinTakeProfit = isSell ? deal.getOpenPrice().subtract(standardMoneyDeltaWithLag) : deal.getOpenPrice().add(standardMoneyDeltaWithLag);
        int compareProfit = lastPriceValue.compareTo(dealPriceWithMinTakeProfit);
        if ((isBuy && compareProfit < 0) || (isSell && compareProfit > 0)) {
            deal.setTakeProfitPrice(ZERO);
        }
    }

    private void closeProfitDeal(Deal deal) {
        if (deal.getTakeProfitPrice().compareTo(ZERO) == 0) {
            return;
        }

        boolean isBuy = deal.getType().equals(OPERATION_TYPE_BUY);
        boolean takeProfitNotBrakeIsBuy = isBuy && lastPriceValue.compareTo(deal.getTakeProfitPrice()) > 0;
        if (takeProfitNotBrakeIsBuy) {
            return;
        }

        boolean isSell = deal.getType().equals(OPERATION_TYPE_SELL);
        boolean takeProfitNotBrakeIsSell = isSell && lastPriceValue.compareTo(deal.getTakeProfitPrice()) < 0;
        if (takeProfitNotBrakeIsSell) {
            return;
        }

        OrderDirection direction = DirectionMapper.mapRevert(deal.getType());
        lastProfitOrderDirection = DirectionMapper.map(deal.getType());
        marketContext.postOrder(mainShare.uid(), 1L, lastPriceValue, direction, deal.getAccountId(), ORDER_TYPE_LIMIT);
    }

    private void closeFrozenOrders(List<Order> activeOrders) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        for (Order order : activeOrders) {
            long diff = ChronoUnit.SECONDS.between(order.date(), now);
            boolean isFrozen = diff > NEW_REQUEST_FROZEN_SECONDS;
            if (isFrozen) {
                marketContext.cancelOrder(order.orderId(), order.accountId());
            }

        }
    }

    private void postNewOrder(List<Deal> deals) {
        long unbalancedCount = deals.stream()
                .filter(deal -> deal.getTakeProfitPrice().compareTo(ZERO) == 0)
                .map(deal -> deal.getType().equals(OPERATION_TYPE_SELL) ? -1 * deal.getQuantity() : deal.getQuantity())
                .mapToLong(value -> value)
                .sum();
        BigDecimal unbalancedSum = deals.stream()
                .filter(deal -> deal.getTakeProfitPrice().compareTo(ZERO) == 0)
                .map(deal -> deal.getType().equals(OPERATION_TYPE_SELL) ? deal.getOpenPrice().subtract(lastPriceValue).multiply(BigDecimal.valueOf(deal.getQuantity())).negate() :
                        lastPriceValue.subtract(deal.getOpenPrice()).multiply(BigDecimal.valueOf(deal.getQuantity())))
                .reduce(BigDecimal::add).orElse(ZERO);
        if (unbalancedSum.compareTo(ZERO) < 0) { // unbalancedCount > 0 && Если покупок больше и они минусе
            postOrderWithRepeatProtected(mainShare.uid(), 1L, ZERO, ORDER_DIRECTION_SELL, ACCOUNT_SELL, ORDER_TYPE_MARKET);
        } else if (unbalancedSum.compareTo(ZERO) > 0) { // unbalancedCount < 0 продаж больше и они в минусе
            postOrderWithRepeatProtected(mainShare.uid(), 1L, ZERO, ORDER_DIRECTION_BUY, ACCOUNT_BUY, ORDER_TYPE_MARKET);
        } else {
            //if (unbalancedCount != 0) return;
            String accountId = lastProfitOrderDirection.equals(ORDER_DIRECTION_SELL) ? ACCOUNT_SELL : ACCOUNT_BUY;
            postOrderWithRepeatProtected(mainShare.uid(), 1L, ZERO, lastProfitOrderDirection, accountId, ORDER_TYPE_MARKET);
        }
    }

    private boolean isEmptyLevel(OrderDirection testDirection) {
        BigDecimal emptyDelta = lastPriceValue.multiply(EMPTY_LEVEL_DELTA).divide(HUNDRED, 9, HALF_UP);
        BigDecimal boundUp = lastPriceValue.add(emptyDelta);
        BigDecimal boundDown = lastPriceValue.subtract(emptyDelta);
        List<Deal> directedDeals = portfolio.getOpenDeals().stream()
                .filter(deal -> deal.getType().equals(DirectionMapper.map(testDirection)))
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
        if (!isEmptyLevel(direction)) {
            return;
        }
        if (enableDelay && ChronoUnit.SECONDS.between(lastAction, LocalDateTime.now(ZoneOffset.UTC)) < 15) {
            return;
        }
        marketContext.postOrder(instrumentId, lot, price, direction, accountId, type);
        lastAction = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void closeDayIfNeed(Event event) {
        if (!CLOSE_DAY.equals(event)) {
            return;
        }
        for (Deal d : this.portfolio.getOpenDeals()) {
            OrderDirection direction = DirectionMapper.mapRevert(d.getType());
            marketContext.postOrder(d.getInstrumentUid(), d.getQuantity() / mainShare.lot(), ZERO, direction, d.getAccountId(), ORDER_TYPE_MARKET);
        }
//
//        BigDecimal profit = marketContext.getClosedDeals(LocalDateTime.now(), LocalDateTime.now())
//                .stream()
//                .map(d -> FinUtils.getProfitMoney(d.getOpenPrice(), d.getClosePrice(), d.getType())
//                        .multiply(BigDecimal.valueOf(d.getQuantity())).add(new BigDecimal("-0.25"))
//                )
//                .reduce(BigDecimal::add).orElse(ZERO);
//        String format = String.format("%s %s %s %s", profit, PERCENT_DELTA_PROFIT, PERCENT_DELTA_PROFIT_TRIGGER, PERCENT_PROFIT_CLEAR_LAG);
//        try(FileWriter fw = new FileWriter("c:\\temp\\s.txt", true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            PrintWriter out = new PrintWriter(bw))
//        {
//            out.println(format);
//        } catch (IOException e) {
//            //exception handling left as an exercise for the reader
//        }
    }
}