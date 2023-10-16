package darling.repository;

import darling.domain.OpenDeal;

import java.util.ArrayList;
import java.util.List;

public class DealRepository {

    private final List<OpenDeal> openDeals = new ArrayList<>();

    public List<OpenDeal> findAll() {
        return openDeals;
    }

    public void saveAll(List<OpenDeal> openDeals) {

    }
}