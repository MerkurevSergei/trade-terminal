package darling.service.sand;

import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.service.MarketDataService;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MarketDataSandService implements MarketDataService {
    @Override
    public Optional<LastPrice> getLastPrice(String instrumentUid) {
        return Optional.empty();
    }

    @Override
    public List<LastPrice> getLastPrices() {
        return List.of();
    }

    @Override
    public void syncLastPrices(List<MainShare> shares) {

    }

    @Override
    public List<HistoricCandle> getCandles(String instrumentUid, Instant from, Instant to, CandleInterval candleIntervalDay) {
        return List.of();
    }
}
