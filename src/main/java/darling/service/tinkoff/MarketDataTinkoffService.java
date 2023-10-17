package darling.service.tinkoff;

import darling.domain.LastPrice;
import darling.domain.Share;
import darling.repository.LastPriceRepository;
import darling.service.MarketDataService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record MarketDataTinkoffService(LastPriceRepository lastPriceRepository,
                                       ru.tinkoff.piapi.core.MarketDataService marketDataService) implements MarketDataService {

    @Override
    public Map<String, LastPrice> getLastPrices() {
        return lastPriceRepository.findAll().stream().collect(Collectors.toMap(LastPrice::instrumentUid, Function.identity()));
    }

    @Override
    public void syncLastPrices(List<Share> shares) {

    }
}