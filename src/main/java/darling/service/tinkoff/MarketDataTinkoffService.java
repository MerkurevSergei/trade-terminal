package darling.service.tinkoff;

import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.mapper.LastPriceMapper;
import darling.repository.LastPriceRepository;
import darling.service.MarketDataService;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record MarketDataTinkoffService(LastPriceRepository lastPriceRepository,
                                       ru.tinkoff.piapi.core.MarketDataService marketDataService) implements MarketDataService {

    @Override
    public Optional<LastPrice> getLastPrice(String instrumentUid) {
        return lastPriceRepository.findByInstrumentUid(instrumentUid);
    }

    @Override
    public List<LastPrice> getLastPrices() {
        return lastPriceRepository.findAll();
    }

    @Override
    public void syncLastPrices(List<MainShare> shares) {
        List<String> shareIds = shares.stream().map(MainShare::uid).toList();
        List<LastPrice> lastPrices = marketDataService.getLastPricesSync(shareIds).stream().map(LastPriceMapper.INST::map).toList();
        lastPriceRepository.saveAll(lastPrices);
    }

    @Override
    public List<HistoricCandle> getCandles(String instrumentUid, Instant from, Instant to, CandleInterval candleIntervalDay) {
        return marketDataService.getCandles(instrumentUid, from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }
}