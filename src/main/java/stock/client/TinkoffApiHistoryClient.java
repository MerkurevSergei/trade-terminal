package stock.client;

import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;
import stock.domain.model.HistoricPoint;
import stock.shared.BeanRegister;
import stock.shared.DateTimeMapper;
import stock.shared.Utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TinkoffApiHistoryClient implements HistoryClient {

    private final InvestApi tinkoffClient = BeanRegister.TINKOFF_CLIENT;

    @Override
    public List<HistoricCandle> getDailyCandles(String figi, LocalDate start, LocalDate end) {
        Instant from = Instant.ofEpochSecond(start.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        Instant to = Instant.ofEpochSecond(end.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        return tinkoffClient.getMarketDataService().getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }

    @Override
    public List<HistoricPoint> getDailyPoints(String figi, LocalDate start, LocalDate end) {
        List<HistoricPoint> points = new ArrayList<>();
        List<HistoricCandle> dailyCandles = getDailyCandles(figi, start, end);
        for (HistoricCandle candle : dailyCandles) {
            LocalDateTime time = DateTimeMapper.map(candle.getTime());
            BigDecimal open = Utils.quotationToBigDecimal(candle.getOpen());
            BigDecimal high = Utils.quotationToBigDecimal(candle.getHigh());
            BigDecimal low = Utils.quotationToBigDecimal(candle.getLow());
            long volume = candle.getVolume();
            points.add(new HistoricPoint(time, open, volume / 3));
            points.add(new HistoricPoint(time, high, volume / 3));
            points.add(new HistoricPoint(time, low, volume / 3));
        }
        return points;
    }
}
