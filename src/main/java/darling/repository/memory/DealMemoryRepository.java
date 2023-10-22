package darling.repository.memory;

import darling.domain.Deal;
import darling.repository.DealRepository;

import java.time.LocalDateTime;
import java.util.List;

public class DealMemoryRepository implements DealRepository {
    @Override
    public List<Deal> findAllOpenDeals() {
        return List.of();
    }

    @Override
    public void refreshOpenDeals(List<Deal> deals) {

    }

    @Override
    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return List.of();
    }

    @Override
    public void saveClosedDeals(List<Deal> closedDeals) {

    }
}
