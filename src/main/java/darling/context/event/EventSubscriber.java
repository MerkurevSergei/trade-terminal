package darling.context.event;

import java.util.ArrayList;
import java.util.List;

public abstract class EventSubscriber {

    private final List<EventListener> listeners = new ArrayList<>();

    public void addListener(EventListener eventListener) {
        listeners.add(eventListener);
    }

    public void removeListener(EventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void notify(Event event) {
        listeners.forEach(it -> it.handle(event));
    }
}