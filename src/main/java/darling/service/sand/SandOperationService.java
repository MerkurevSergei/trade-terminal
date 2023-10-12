package darling.service.sand;

import darling.context.event.EventListener;
import darling.service.OperationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SandOperationService implements OperationService {

    private final List<EventListener> listeners = new ArrayList<>();

    @Override
    public void refreshOperations(LocalDateTime start, LocalDateTime end) {
        listeners.forEach(it -> it.handle(new ArrayList<>()));
    }

    @Override
    public void addListener(EventListener eventListener) {
        listeners.add(eventListener);
    }
}
