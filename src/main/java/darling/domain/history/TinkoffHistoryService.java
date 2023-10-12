package darling.domain.history;

import darling.shared.TinkoffTypeMapper;
import darling.context.SandMarketContext;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TinkoffHistoryService implements HistoryService {

    private static final InvestApi tinkoffClient = SandMarketContext.TINKOFF_CLIENT;

    @Override
    public List<HistoricCandle> getDailyCandles(String figi, LocalDateTime start, LocalDateTime end) {
        Instant from = Instant.ofEpochSecond(start.toEpochSecond(ZoneOffset.UTC));
        Instant to = Instant.ofEpochSecond(end.toEpochSecond(ZoneOffset.UTC));
        return tinkoffClient.getMarketDataService().getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }

    @Override
    public List<HistoricPoint> getDailyPoints(String figi, LocalDateTime start, LocalDateTime end) {
        List<HistoricPoint> points = new ArrayList<>();
        List<HistoricCandle> dailyCandles = getDailyCandles(figi, start, end);
        for (HistoricCandle candle : dailyCandles) {
            LocalDateTime time = TinkoffTypeMapper.map(candle.getTime());
            BigDecimal open = TinkoffTypeMapper.map(candle.getOpen());
            BigDecimal high = TinkoffTypeMapper.map(candle.getHigh());
            BigDecimal low = TinkoffTypeMapper.map(candle.getLow());
            long volume = candle.getVolume();
            points.add(new HistoricPoint(time, open, volume / 3));
            points.add(new HistoricPoint(time, high, volume / 3));
            points.add(new HistoricPoint(time, low, volume / 3));
        }
        return points;
    }

    @Override
    public List<HistoricCandle> getMinuteCandles(String figi, LocalDateTime start, LocalDateTime end) {
        ArrayList<HistoricCandle> candles = new ArrayList<>();
        for (LocalDateTime i = start; i.isBefore(end); i = i.plusDays(1L)) {
            Instant from = Instant.ofEpochSecond(i.toEpochSecond(ZoneOffset.UTC));
            Instant to = Instant.ofEpochSecond(i.plusDays(1L).toEpochSecond(ZoneOffset.UTC));
            List<HistoricCandle> mins = tinkoffClient.getMarketDataService().getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_1_MIN).join();
            candles.addAll(mins);
        }
        return candles;
    }

    @Override
    public List<HistoricPoint> getMinutePointsByDay(String figi, LocalDateTime start, LocalDateTime end) {
        List<HistoricPoint> points = new ArrayList<>();
        List<HistoricCandle> minuteCandles = getMinuteCandles(figi, start, end);
        for (HistoricCandle candle : minuteCandles) {
            LocalDateTime time = TinkoffTypeMapper.map(candle.getTime());
            BigDecimal open = TinkoffTypeMapper.map(candle.getOpen());
            BigDecimal high = TinkoffTypeMapper.map(candle.getHigh());
            BigDecimal low = TinkoffTypeMapper.map(candle.getLow());
            long volume = candle.getVolume();
            points.add(new HistoricPoint(time.plusSeconds(1), open, volume / 3));
            points.add(new HistoricPoint(time.plusSeconds(2), high, volume / 3));
            points.add(new HistoricPoint(time.plusSeconds(3), low, volume / 3));
        }
        return points;
    }
}
