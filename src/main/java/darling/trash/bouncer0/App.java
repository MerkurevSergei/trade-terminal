package darling.trash.bouncer0;

import darling.context.BeanFactory;
import darling.domain.HistoricCandle;
import darling.domain.HistoricPoint;
import darling.service.HistoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.LocalTime.MAX;
import static ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN;

public class App {

    private static final String INSTRUMENT_UID = "8e2b0325-0292-4654-8a18-4f63ed3b0e09";

    private static HistoricCandle lastBar;
    private static int upTwice = 0;
    private static int downTwice = 0;
    private static UpBound upBoundLast;
    private static DownBound downBoundLast;

    public static void main(String[] args) {
        LocalDate currentDay = LocalDate.of(2023, Month.NOVEMBER, 17);
        LocalDate prevDay = LocalDate.of(2023, Month.NOVEMBER, 16);

        BeanFactory beanFactory = new BeanFactory(true);
        HistoryService historyService = beanFactory.getHistoryService();

        Atr5 atr5 = new Atr5(historyService, prevDay, INSTRUMENT_UID);
        Map<LocalDateTime, HistoricCandle> bars = historyService.getCandles(INSTRUMENT_UID, currentDay.atStartOfDay(),
                                                                            currentDay.atTime(MAX), CANDLE_INTERVAL_5_MIN)
                .stream()
                .collect(Collectors.toMap(HistoricCandle::time, Function.identity()));

        List<HistoricPoint> points = historyService.getMinutePointsByDay(INSTRUMENT_UID, currentDay.atStartOfDay(),
                                                                         currentDay.atTime(MAX));
        UpBound upBound = new UpBound();
        DownBound downBound = new DownBound();
        for (HistoricPoint point : points) {
            HistoricCandle lastMakerBar = getLastBar(bars, point.time());
            if (lastMakerBar == null || lastBar == lastMakerBar) continue;
            lastBar = lastMakerBar;
            HistoricCandle lastNormalBar = atr5.normalize(lastMakerBar);
            atr5.add(lastNormalBar);

            upBound.update(lastNormalBar, atr5);
            downBound.update(lastNormalBar, atr5);

            printUpBound(upBound, point);
            printDownBound(downBound, point);
        }

    }

    private static HistoricCandle getLastBar(Map<LocalDateTime, HistoricCandle> bars, LocalDateTime time) {
        int minutes = 5 + time.getMinute() % 5;
        time = time.withSecond(0).withNano(0).minusMinutes(minutes);
        return bars.get(time);
    }

    private static boolean priceOnUpBound(UpBound upBound, BigDecimal price) {
        if (!upBound.isVerified()) return false;
        BigDecimal upPrice = upBound.getPoint().price();
        BigDecimal downPrice = upPrice.multiply(new BigDecimal("0.9990"));
        return price.compareTo(upPrice) <= 0 && price.compareTo(downPrice) >= 0;
    }

    private static boolean priceOnDownBound(DownBound downBound, BigDecimal price) {
        if (!downBound.isVerified()) return false;
        BigDecimal downPrice = downBound.getPoint().price();
        BigDecimal upPrice = downPrice.multiply(new BigDecimal("1.0010"));
        return price.compareTo(upPrice) <= 0 && price.compareTo(downPrice) >= 0;
    }

    private static void printUpBound(UpBound upBound, HistoricPoint point) {
        if (priceOnUpBound(upBound, point.price())) {
            upTwice++;
            if (upTwice < 1) return;
            System.out.println("UP");
            System.out.println(point);
            System.out.println(upBound);
        } else {
            upTwice = 0;
        }
    }

    private static void printDownBound(DownBound downBound, HistoricPoint point) {
        if (priceOnDownBound(downBound, point.price())) {
            downTwice++;
            if (downTwice < 1) return;
            System.out.println("DOWN");
            System.out.println(point);
            System.out.println(downBound);
        } else {
            downTwice = 0;
        }
    }

}
