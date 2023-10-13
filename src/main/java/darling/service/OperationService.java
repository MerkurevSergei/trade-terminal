package darling.service;

import darling.context.event.EventListener;
import darling.domain.operations.model.Operation;

import java.util.List;

public interface OperationService {

    List<Operation> getAll();

    void sync();

    void addListener(EventListener eventListener);
}