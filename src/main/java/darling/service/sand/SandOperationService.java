package darling.service.sand;

import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.operations.model.Operation;
import darling.service.OperationService;

import java.util.ArrayList;
import java.util.List;

public class SandOperationService implements OperationService {

    private final List<EventListener> listeners = new ArrayList<>();

    @Override
    public List<Operation> getAll() {
        return null;
    }

    @Override
    public void sync() {
        listeners.forEach(it -> it.handle(Event.OPERATION_UPDATED));
    }

    @Override
    public void addListener(EventListener eventListener) {
        listeners.add(eventListener);
    }
}
