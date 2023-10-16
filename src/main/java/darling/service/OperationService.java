package darling.service;

import darling.domain.Operation;
import darling.domain.Position;

import java.util.List;

public interface OperationService {

    List<Position> getAllPositions();

    void syncPositions();

    List<Operation> getAllOperations();

    boolean syncOperations();
}