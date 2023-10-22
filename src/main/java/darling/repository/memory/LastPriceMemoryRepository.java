package darling.repository.memory;

import darling.domain.LastPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record LastPriceMemoryRepository(List<LastPrice> lastPrices) {

    public void saveAll(List<LastPrice> lastPrices) {
        this.lastPrices.clear();
        this.lastPrices.addAll(lastPrices);
    }

    public List<LastPrice> findAll() {
        return new ArrayList<>(this.lastPrices);
    }

    public Optional<LastPrice> findByInstrumentUid(String instrumentUid) {
        return lastPrices.stream()
                .filter(lastPrice -> Objects.equals(lastPrice.instrumentUid(), instrumentUid))
                .findFirst();
    }
}