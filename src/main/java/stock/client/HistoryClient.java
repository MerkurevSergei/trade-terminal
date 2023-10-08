package stock.client;

import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;
import stock.shared.BeanRegister;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class HistoryClient {

    private final InvestApi tinkoffClient = BeanRegister.TINKOFF_CLIENT;

    public List<HistoricCandle> loadHistoryDay(String figi, LocalDate start, LocalDate end) {
        Instant from = Instant.ofEpochSecond(start.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        Instant to = Instant.ofEpochSecond(end.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
        return tinkoffClient.getMarketDataService().getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }
}
