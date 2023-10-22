package darling.repository;

import darling.domain.Operation;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationRepository {

    List<Operation> findAll();

    int saveNew(List<Operation> operations);

    LocalDateTime getLastOperationTime(String account);

    List<Operation> popFromQueue();
}
