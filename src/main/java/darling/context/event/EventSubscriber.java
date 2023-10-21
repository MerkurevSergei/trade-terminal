package darling.context.event;

import java.util.ArrayList;
import java.util.List;

public abstract class EventSubscriber {

    private final List<EventListener> listeners = new ArrayList<>();
    private final boolean sandMode;

    public EventSubscriber(boolean sandMode) {
        this.sandMode = sandMode;
    }

    public void addListener(EventListener eventListener) {
        listeners.add(eventListener);
    }

    public void notifyLive(Event event) {
        if (sandMode) return;
        listeners.forEach(it -> it.handle(event));
    }

    public void notifySand(Event event) {
        if (!sandMode) return;
        listeners.forEach(it -> it.handle(event));
    }
}
