package darling.repository.memory;

import darling.domain.Operation;
import darling.repository.OperationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperationMemoryRepository implements OperationRepository {

    private final List<Operation> operations = new ArrayList<>();

    @Override
    public List<Operation> findAll() {
        return new ArrayList<>(operations);
    }

    @Override
    public int saveNew(List<Operation> operations) {
        return 0;
    }

    @Override
    public LocalDateTime getLastOperationTime(String account) {
        return LocalDateTime.now();
    }

    @Override
    public List<Operation> popFromQueue() {
        return List.of();
    }
}
