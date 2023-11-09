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

import static java.time.LocalTime.MAX;
import static ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN;

public class App {

    private static final String INSTRUMENT_UID = "8e2b0325-0292-4654-8a18-4f63ed3b0e09";

    public static void main(String[] args) {
        LocalDate currentDay = LocalDate.of(2023, Month.NOVEMBER, 1);

        BeanFactory beanFactory = new BeanFactory(true);
        HistoryService historyService = beanFactory.getHistoryService();

        Atr5 atr5 = new Atr5(historyService, currentDay.minusDays(1), INSTRUMENT_UID);
        List<HistoricCandle> bars5 = historyService.getCandles(INSTRUMENT_UID, currentDay.atStartOfDay(),
                                                               currentDay.atTime(MAX), CANDLE_INTERVAL_5_MIN);

        List<HistoricPoint> points = historyService.getMinutePointsByDay(INSTRUMENT_UID, currentDay.atStartOfDay(),
                                                                         currentDay.atTime(MAX));
        for (HistoricPoint point : points) {
            HistoricCandle last5Bar = getLast5Bar(bars5, point.time());
            BigDecimal last5BarLength = last5Bar.high().subtract(last5Bar.low());
            if (!atr5.isStandardLength(last5BarLength)) {
                return;
            }
            atr5.add(last5BarLength);
        }

    }

    private static HistoricCandle getLast5Bar(List<HistoricCandle> bars5, LocalDateTime time) {
        return null;
    }
}
