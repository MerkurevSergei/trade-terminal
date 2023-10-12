package darling.service;

import darling.context.event.EventListener;

import java.time.LocalDateTime;

public interface OperationService {
    void refreshOperations(LocalDateTime start, LocalDateTime end);

    void addListener(EventListener eventListener);
}