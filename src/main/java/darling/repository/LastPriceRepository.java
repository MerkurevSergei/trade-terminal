package darling.repository;

import darling.domain.LastPrice;

import java.util.ArrayList;
import java.util.List;

public record LastPriceRepository(List<LastPrice> lastPrices) {

    public void saveAll(List<LastPrice> lastPrices) {
        this.lastPrices.clear();
        this.lastPrices.addAll(lastPrices);
    }

    public List<LastPrice> findAll() {
        return new ArrayList<>(this.lastPrices);
    }
}
