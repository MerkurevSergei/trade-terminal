package darling.service.common;

import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.mapper.LastPriceMapper;
import darling.repository.memory.LastPriceMemoryRepository;
import darling.service.LastPriceService;

import java.util.List;
import java.util.Optional;

public record LastPriceTinkoffService(LastPriceMemoryRepository lastPriceMemoryRepository,
                                      ru.tinkoff.piapi.core.MarketDataService marketDataService) implements LastPriceService {

    @Override
    public Optional<LastPrice> getLastPrice(String instrumentUid) {
        return lastPriceMemoryRepository.findByInstrumentUid(instrumentUid);
    }

    @Override
    public List<LastPrice> getLastPrices() {
        return lastPriceMemoryRepository.findAll();
    }

    @Override
    public void syncLastPrices(List<MainShare> shares) {
        List<String> shareIds = shares.stream().map(MainShare::uid).toList();
        List<LastPrice> lastPrices = marketDataService.getLastPricesSync(shareIds).stream().map(LastPriceMapper.INST::map).toList();
        lastPriceMemoryRepository.saveAll(lastPrices);
    }
}