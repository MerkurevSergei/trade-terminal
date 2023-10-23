package darling.service.common;

import darling.domain.HistoricPoint;
import darling.domain.LastPrice;
import darling.mapper.TinkoffSpecialTypeMapper;
import darling.repository.memory.LastPriceMemoryRepository;
import darling.service.HistoryService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class HistoryTinkoffService implements HistoryService {

    private final LastPriceMemoryRepository lastPriceRepository;

    private final MarketDataService marketDataService;

    @Override
    public List<HistoricCandle> getCandles(String instrumentUid, Instant from, Instant to, CandleInterval candleInterval) {
        return marketDataService.getCandles(instrumentUid, from, to, candleInterval).join();
    }

    @Override
    public List<HistoricCandle> getDailyCandles(String figi, LocalDateTime start, LocalDateTime end) {
        Instant from = Instant.ofEpochSecond(start.toEpochSecond(ZoneOffset.UTC));
        Instant to = Instant.ofEpochSecond(end.toEpochSecond(ZoneOffset.UTC));
        return getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_DAY);
    }

    @Override
    public List<HistoricPoint> getDailyPoints(String figi, LocalDateTime start, LocalDateTime end) {
        List<HistoricPoint> points = new ArrayList<>();
        List<HistoricCandle> dailyCandles = getDailyCandles(figi, start, end);
        for (HistoricCandle candle : dailyCandles) {
            LocalDateTime time = TinkoffSpecialTypeMapper.map(candle.getTime());
            BigDecimal open = TinkoffSpecialTypeMapper.map(candle.getOpen());
            BigDecimal high = TinkoffSpecialTypeMapper.map(candle.getHigh());
            BigDecimal low = TinkoffSpecialTypeMapper.map(candle.getLow());
            long volume = candle.getVolume();
            points.add(new HistoricPoint(time, open, volume / 3));
            points.add(new HistoricPoint(time, high, volume / 3));
            points.add(new HistoricPoint(time, low, volume / 3));
        }
        return points;
    }

    @Override
    public List<HistoricCandle> getMinuteCandles(String instrumentUid, LocalDateTime start, LocalDateTime end) {
        ArrayList<HistoricCandle> candles = new ArrayList<>();
        for (LocalDateTime i = start; i.isBefore(end); i = i.plusDays(1L)) {
            Instant from = Instant.ofEpochSecond(i.toEpochSecond(ZoneOffset.UTC));
            Instant to = Instant.ofEpochSecond(i.toLocalDate().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC));
            List<HistoricCandle> mins = getCandles(instrumentUid, from, to, CandleInterval.CANDLE_INTERVAL_1_MIN);
            candles.addAll(mins);
        }
        return candles;
    }

    @Override
    public List<HistoricPoint> getMinutePointsByDay(String figi, LocalDateTime start, LocalDateTime end) {
        List<HistoricPoint> points = new ArrayList<>();
        List<HistoricCandle> minuteCandles = getMinuteCandles(figi, start, end);
        for (HistoricCandle candle : minuteCandles) {
            LocalDateTime time = TinkoffSpecialTypeMapper.map(candle.getTime());
            BigDecimal open = TinkoffSpecialTypeMapper.map(candle.getOpen());
            BigDecimal high = TinkoffSpecialTypeMapper.map(candle.getHigh());
            BigDecimal low = TinkoffSpecialTypeMapper.map(candle.getLow());
            long volume = candle.getVolume();
            points.add(new HistoricPoint(time.plusSeconds(1), open, volume / 3));
            points.add(new HistoricPoint(time.plusSeconds(2), high, volume / 3));
            points.add(new HistoricPoint(time.plusSeconds(3), low, volume / 3));
        }
        points.sort(Comparator.comparing(HistoricPoint::time));
        return points;
    }

    @Override
    public void updateLastPrice(LastPrice lastPrice) {
        lastPriceRepository.saveAll(List.of(lastPrice));
    }

}
