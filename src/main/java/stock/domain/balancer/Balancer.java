package stock.domain.balancer;

import lombok.RequiredArgsConstructor;
import stock.client.HistoryClient;
import stock.domain.model.Bet;
import stock.domain.model.HistoricPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static stock.domain.model.Bet.Direction.DOWN;
import static stock.domain.model.Bet.Direction.UP;

@RequiredArgsConstructor
public final class Balancer {

    private final HistoryClient historyClient;

    private final List<Bet> bets = new ArrayList<>();

    private BigDecimal revenueDelta;

    private final BigDecimal profit = BigDecimal.ZERO;

    public Map<LocalDate, BigDecimal> getProfit(String figi, LocalDate from, LocalDate to) {
        List<HistoricPoint> points = historyClient.getDailyPoints(figi, from, to);
        if (points.isEmpty()) {
            return Map.of();
        }
        this.revenueDelta = getRevenueDelta(points.get(0));
        points.forEach(this::makeBet);
//        closeStopLoss();
//        closeMarket();
        return Map.of(LocalDate.now(), BigDecimal.ONE);
    }

    private BigDecimal getRevenueDelta(HistoricPoint historicPoint) {
        int scale = historicPoint.price().scale();
        return historicPoint.price().divide(BigDecimal.valueOf(500), scale, RoundingMode.HALF_UP);
    }

    private void makeBet(HistoricPoint currentPoint) {
        makeFirstIsNeed(currentPoint);
        Bet lastBet = bets.get(bets.size() - 1);
        Bet.Direction lastBetDirection = lastBet.getDirection();
        BigDecimal revenue = currentPoint.price().subtract(lastBet.getPrice());
        revenue = lastBetDirection.equals(DOWN) ? revenue.negate() : revenue;

        if (revenue.compareTo(revenueDelta) > 0) {
            Bet newBet = new Bet(currentPoint.time(), currentPoint.price(), lastBetDirection);
            bets.add(newBet);
        } else if (revenue.compareTo(revenueDelta) < 0) {
            Bet.Direction reverseDirection = lastBetDirection.equals(UP) ? DOWN : UP;
            Bet newBet = new Bet(currentPoint.time(), currentPoint.price(), reverseDirection);
            bets.add(newBet);
        }
    }

    private void makeFirstIsNeed(HistoricPoint historicPoint) {
        if (bets.isEmpty()) {
            Bet bet = new Bet(historicPoint.time(), historicPoint.price(), Bet.Direction.UP);
            bets.add(bet);
        }
    }
}
