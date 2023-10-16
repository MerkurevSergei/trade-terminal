package darling.repository;

import darling.domain.Contract;

import java.util.ArrayList;
import java.util.List;

public class ContractRepository {

    private final List<Contract> contracts = new ArrayList<>();

    public List<Contract> findAll() {
        return contracts;
    }

    public void saveAll(List<Contract> contracts) {

    }
}