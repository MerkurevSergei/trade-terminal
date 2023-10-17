package darling.service;

import darling.domain.LastPrice;
import darling.domain.Share;

import java.util.List;
import java.util.Map;

public interface MarketDataService {
    Map<String, LastPrice> getLastPrices();

    void syncLastPrices(List<Share> shares);
}
