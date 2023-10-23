package darling.repository.memory;

import darling.domain.Operation;
import darling.repository.OperationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OperationMemoryRepository implements OperationRepository {

    private final Set<Operation> operationAll = new HashSet<>();
    private final Set<Operation> operationQueue = new HashSet<>();

    @Override
    public List<Operation> findAll() {
        List<Operation> result = new ArrayList<>(this.operationAll);
        result.sort(Comparator.comparing(Operation::date));
        return result;
    }

    @Override
    public int saveNew(List<Operation> operations) {
        this.operationAll.addAll(operations);
        this.operationQueue.addAll(operations);
        return this.operationQueue.size();
    }

    @Override
    public LocalDateTime getLastOperationTime(String account) {
        return LocalDateTime.now();
    }

    @Override
    public List<Operation> popFromQueue() {
        List<Operation> result = new ArrayList<>(this.operationQueue);
        this.operationQueue.clear();
        result.sort(Comparator.comparing(Operation::date));
        return result;
    }
}
