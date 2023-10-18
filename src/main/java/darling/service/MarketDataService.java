package darling.service;

import darling.domain.LastPrice;
import darling.domain.Share;

import java.util.List;
import java.util.Optional;

public interface MarketDataService {
    Optional<LastPrice> getLastPrice(String instrumentUid);

    List<LastPrice> getLastPrices();

    void syncLastPrices(List<Share> shares);
}
