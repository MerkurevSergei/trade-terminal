package darling.repository;

import darling.domain.Deal;

import java.time.LocalDateTime;
import java.util.List;

public interface DealRepository {
    List<Deal> findAllOpenDeals();

    void refreshOpenDeals(List<Deal> deals);

    List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end);

    void saveClosedDeals(List<Deal> closedDeals);
}
