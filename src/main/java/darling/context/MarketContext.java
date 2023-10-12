package darling.context;

import darling.context.event.EventListener;

public interface MarketContext {

    void start();

    void stop();

    void addOperationListener(EventListener eventListener);
}
