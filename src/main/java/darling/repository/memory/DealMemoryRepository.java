package darling.repository.memory;

import darling.domain.Deal;
import darling.repository.DealRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DealMemoryRepository implements DealRepository {

    private final List<Deal> openDeals = new ArrayList<>();
    private final List<Deal> closedDeals = new ArrayList<>();

    @Override
    public List<Deal> findAllOpenDeals() {
        return new ArrayList<>(this.openDeals);
    }

    @Override
    public void clearAndSaveOpenDeals(List<Deal> deals) {
        this.openDeals.clear();
        this.openDeals.addAll(deals);
    }

    @Override
    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return this.closedDeals;
    }

    @Override
    public void saveClosedDeals(List<Deal> closedDeals) {
        this.closedDeals.addAll(closedDeals);
    }
}
