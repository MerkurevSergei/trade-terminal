package darling.domain.positions;

import darling.context.event.EventListener;

import java.time.LocalDateTime;

public interface OperationsService {
    void refreshOperations(LocalDateTime start, LocalDateTime end);

    void addListener(EventListener eventListener);
}