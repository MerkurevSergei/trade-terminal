package darling.service;

import darling.domain.Operation;
import darling.domain.Position;

import java.util.List;

public interface OperationService {

    List<Operation> getAllOperations();

    List<Position> getAllPositions();

    boolean syncOperations();

    void syncPositions();
}