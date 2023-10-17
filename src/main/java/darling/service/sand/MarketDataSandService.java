package darling.service.sand;

import darling.domain.LastPrice;
import darling.domain.Share;
import darling.service.MarketDataService;

import java.util.List;
import java.util.Map;

public class MarketDataSandService implements MarketDataService {
    @Override
    public Map<String, LastPrice> getLastPrices() {
        return Map.of();
    }

    @Override
    public void syncLastPrices(List<Share> shares) {

    }
}
