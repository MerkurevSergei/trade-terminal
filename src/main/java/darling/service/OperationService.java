package darling.service;

import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.domain.Position;

import java.util.List;

public interface OperationService {

    List<Operation> getAllOperations();

    List<Position> getAllPositions();

    void syncOperations();

    void syncPositions();

    void addListener(EventListener eventListener);
}