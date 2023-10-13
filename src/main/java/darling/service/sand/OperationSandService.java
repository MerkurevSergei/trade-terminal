package darling.service.sand;

import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.domain.Position;
import darling.service.OperationService;

import java.util.ArrayList;
import java.util.List;

public class OperationSandService implements OperationService {

    private final List<EventListener> listeners = new ArrayList<>();

    @Override
    public List<Operation> getAllOperations() {
        return List.of();
    }

    @Override
    public List<Position> getAllPositions() {
        return List.of();
    }

    @Override
    public void syncOperations() {
        listeners.forEach(it -> it.handle(Event.OPERATION_UPDATED));
    }

    @Override
    public void syncPositions() {
    }

    @Override
    public void addListener(EventListener eventListener) {
        listeners.add(eventListener);
    }
}
