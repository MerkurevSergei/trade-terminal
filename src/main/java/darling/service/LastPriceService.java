package darling.service;

import darling.domain.LastPrice;
import darling.domain.MainShare;

import java.util.List;
import java.util.Optional;

public interface LastPriceService {
    Optional<LastPrice> getLastPrice(String instrumentUid);

    List<LastPrice> getLastPrices();

    void syncLastPrices(List<MainShare> shares);
}
