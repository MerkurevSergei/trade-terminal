package darling.service;

import darling.domain.LastPrice;
import darling.domain.MainShare;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketDataService {
    Optional<LastPrice> getLastPrice(String instrumentUid);

    List<LastPrice> getLastPrices();

    void syncLastPrices(List<MainShare> shares);

    List<HistoricCandle> getCandles(String instrumentUid, Instant from, Instant to, CandleInterval candleIntervalDay);
}
