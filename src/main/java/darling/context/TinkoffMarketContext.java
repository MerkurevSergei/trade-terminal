package darling.context;

import darling.context.event.EventListener;
import darling.domain.positions.OperationsService;
import darling.domain.positions.TinkoffOperationsService;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.LocalDateTime;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;

public class TinkoffMarketContext implements MarketContext {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    private final OperationsService operationsService;

    public TinkoffMarketContext() {
        this.operationsService = new TinkoffOperationsService();
    }

    @Override
    public void start() {
        operationsService.refreshOperations(LocalDateTime.now().minusDays(30), LocalDateTime.now());
        //sendOperationsRefresh();


    }

//    private void sendOperationsRefresh(Collection<Operation> operations) {
//        for (Listener listener : listeners) {
//            listener.onRefresh(Collection<Operation> operations)
//        }
//    }

    @Override
    public void stop() {

    }

    @Override
    public void addOperationListener(EventListener eventListener) {
        operationsService.addListener(eventListener);
    }
}