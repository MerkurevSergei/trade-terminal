package darling.domain.robot.balancer;

import darling.domain.order.OrderService;
import darling.domain.order.SandOrderService;
import darling.domain.order.model.OrderDirection;
import darling.domain.Bet;
import darling.domain.history.HistoricPoint;
import darling.domain.robot.Robot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public final class Balancer implements Robot {

    private final String instrumentUid;

    private final OrderService orderService = new SandOrderService();

    private final List<Bet> bets = new ArrayList<>();

    private final List<StatRecord> stat = new ArrayList<>();

    private final BigDecimal profitDelta;

    private final BigDecimal levelGap;

    private Integer unstoppable = 0;

    public Balancer(String instrumentUid, BigDecimal profitDelta, BigDecimal levelGap) {
        this.instrumentUid = instrumentUid;
        this.profitDelta = profitDelta;
        this.levelGap = levelGap;
    }

    public List<StatRecord> getProfitSum(List<HistoricPoint> points) {
        if (points.isEmpty()) {
            return stat;
        }

        points.forEach(this::doStep);
        closeMarket(points.get(points.size() - 1));
        return stat;
    }

    /**
     * Шаг рынка.
     *
     * @param currentPoint текущая точка время/цена/объем.
     */
    @Override
    public void doStep(HistoricPoint currentPoint) {
        makeFirstIsNeed(currentPoint);
        setOrTakeProfit(currentPoint);
        boolean isEmptyLevel = isEmptyLevel(currentPoint);
        if (!isEmptyLevel) {
            return;
        }
        makeBet(currentPoint);
    }

    private void setOrTakeProfit(HistoricPoint currentPoint) {
        List<Bet> profitList = new ArrayList<>();
        for (Bet bet : bets) {
            // Устанавливаем тейк профит, если еще не установлен
            if (bet.getTakeProfit().equals(ZERO)) {
                BigDecimal profit = currentPoint.price().subtract(bet.getPrice());
                profit = bet.getDirection().equals(OrderDirection.DOWN) ? profit.negate() : profit;
                if (profit.compareTo(profitDelta) > 0) {
                    BigDecimal takeProfit = bet.getDirection().equals(OrderDirection.DOWN) ? bet.getPrice().subtract(profitDelta) : bet.getPrice().add(profitDelta);
                    bet.setTakeProfit(takeProfit);
                    unstoppable = bet.getDirection().equals(OrderDirection.UP) ? unstoppable - 1 : unstoppable + 1;
                }
            }

            // Если установлен, можем закрыть или переместить
            if (!bet.getTakeProfit().equals(ZERO)) {
                // Закрываем
                BigDecimal profitBetIfPriceBit = bet.getTakeProfit().subtract(bet.getPrice());
                profitBetIfPriceBit = profitBetIfPriceBit.compareTo(ZERO) < 0 ? profitBetIfPriceBit.negate() : profitBetIfPriceBit;
                BigDecimal currentProfit = currentPoint.price().subtract(bet.getPrice());
                currentProfit = bet.getDirection().equals(OrderDirection.DOWN) ? currentProfit.negate() : currentProfit;
                if (currentProfit.compareTo(profitBetIfPriceBit) < 0) {
                    profitList.add(bet);
                    BigDecimal profitPercent = profitBetIfPriceBit.divide(bet.getPrice(), bet.getPrice().scale(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    stat.add(new StatRecord(bet, currentPoint.time(), profitPercent));
                    continue;
                }

                // Перемещаем
                if (currentProfit.add(profitDelta).compareTo(profitBetIfPriceBit) < 0) {
                    bet.setTakeProfit(currentPoint.price().subtract(profitDelta));
                }
            }

        }
        bets.removeAll(profitList);
    }

    private void makeBet(HistoricPoint currentPoint) {
        Bet lastBet = bets.get(bets.size() - 1);
        var lastDirection = lastBet.getDirection();
        BigDecimal revenue = currentPoint.price().subtract(lastBet.getPrice());
        revenue = lastDirection.equals(OrderDirection.DOWN) ? revenue.negate() : revenue;
        if (revenue.compareTo(profitDelta) > 0) {
            if (lastDirection.equals(OrderDirection.UP) && unstoppable > 0) {
                lastDirection = OrderDirection.DOWN;
            }
            if (lastDirection.equals(OrderDirection.DOWN) && unstoppable < 0) {
                lastDirection = OrderDirection.UP;
            }
            orderService.postOrder(instrumentUid, 1, currentPoint.price(), lastDirection, null);
            //            Bet newBet = new Bet(currentPoint.time(), currentPoint.price(), lastDirection);
            //            bets.add(newBet);
            unstoppable = lastDirection.equals(OrderDirection.UP) ? unstoppable + 1 : unstoppable - 1;
        } else if (revenue.compareTo(profitDelta) < 0) {
            OrderDirection reverseDirection = lastDirection.equals(OrderDirection.UP) ? OrderDirection.DOWN : OrderDirection.UP;
            if (reverseDirection.equals(OrderDirection.UP) && unstoppable > 0) {
                reverseDirection = OrderDirection.DOWN;
            }
            if (reverseDirection.equals(OrderDirection.DOWN) && unstoppable < 0) {
                reverseDirection = OrderDirection.UP;
            }
            orderService.postOrder(instrumentUid, 1, currentPoint.price(), reverseDirection, null);
            //            Bet newBet = new Bet(currentPoint.time(), currentPoint.price(), reverseDirection);
            //            bets.add(newBet);
            unstoppable = reverseDirection.equals(OrderDirection.UP) ? unstoppable + 1 : unstoppable - 1;
        }
    }

    private void closeMarket(HistoricPoint lastPoint) {
        for (Bet bet : bets) {
            BigDecimal profit = lastPoint.price().subtract(bet.getPrice());
            profit = bet.getDirection().equals(OrderDirection.DOWN) ? profit.negate() : profit;
            BigDecimal profitPercent = profit.divide(bet.getPrice(), bet.getPrice().scale(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            stat.add(new StatRecord(bet, lastPoint.time(), profitPercent));
        }
    }

    private boolean isEmptyLevel(HistoricPoint currentPoint) {
        BigDecimal newBetHighPrice = currentPoint.price().add(levelGap);
        BigDecimal newBetLowPrice = currentPoint.price().subtract(levelGap);
        for (Bet bet : bets) {
            if (!bet.getTakeProfit().equals(ZERO)) {
                continue;
            }
            BigDecimal activeBetPrice = bet.getPrice();
            BigDecimal oneDelta = activeBetPrice.subtract(newBetHighPrice).abs();
            BigDecimal twoDelta = activeBetPrice.subtract(newBetLowPrice).abs();

            if (oneDelta.compareTo(profitDelta) <= 0) {
                return false;
            }
            if (twoDelta.compareTo(profitDelta) <= 0) {
                return false;
            }
        }
        return true;
    }

    private void makeFirstIsNeed(HistoricPoint historicPoint) {
        if (bets.isEmpty()) {
            orderService.postOrder(instrumentUid, 1, historicPoint.price(), OrderDirection.UP, null);
//            Bet bet = new Bet(historicPoint.time(), historicPoint.price(), UP);
//            bets.add(bet);
        }
    }
}
