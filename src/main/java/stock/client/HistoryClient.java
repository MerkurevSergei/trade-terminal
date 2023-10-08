package stock.client;

import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;
import stock.shared.BeanRegister;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;

public class HistoryClient {

    private final InvestApi tinkoffClient = BeanRegister.TINKOFF_CLIENT;

    public List<HistoricCandle> loadHistory(String figi) {

        LocalDateTime start = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, Month.SEPTEMBER, 30, 23, 59);
        Timestamp timestampStart = Timestamp.newBuilder().setSeconds(start.toEpochSecond(ZoneOffset.UTC)).build();
        Timestamp timestampEnd = Timestamp.newBuilder().setSeconds(end.toEpochSecond(ZoneOffset.UTC)).build();
        Instant from = Instant.ofEpochSecond(timestampStart.getSeconds() + 3600);
        Instant to = Instant.ofEpochSecond(timestampEnd.getSeconds() + 7200);

        return tinkoffClient.getMarketDataService().getCandles(figi, from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }
}
