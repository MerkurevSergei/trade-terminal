package darling.context;

import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.domain.Position;
import darling.domain.Share;
import darling.repository.AvailableShareRepository;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.OperationService;
import darling.service.sand.OperationSandService;
import darling.service.tinkoff.HistoryTinkoffService;
import darling.service.tinkoff.InstrumentTinkoffService;
import darling.service.tinkoff.OperationTinkoffService;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MarketContext {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    private final OperationService operationService;
    private final InstrumentService instrumentService;
    private final ScheduledExecutorService executorService;

    public static final HistoryService HISTORY_SERVICE = new HistoryTinkoffService();


    private final boolean sandMode;

    public static final AvailableShareRepository MAIN_SHARE_REPOSITORY = new AvailableShareRepository();

    public MarketContext(boolean sandMode) {
        this.sandMode = sandMode;
        this.operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(TINKOFF_CLIENT.getOperationsService());
        this.instrumentService = new InstrumentTinkoffService(TINKOFF_CLIENT.getInstrumentsService());
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        int delay = sandMode ? 1 : 2;
        executorService.scheduleWithFixedDelay(() -> {
            operationService.syncPositions();
            operationService.syncOperations();
        }, 1, delay, SECONDS);
    }

    public void stop() {
        executorService.shutdown();
        try {
            boolean isCompleted = executorService.awaitTermination(1, TimeUnit.MINUTES);
            if (!isCompleted) {
                throw new IllegalStateException("Не удалось корректно переключить режим. Пожалуйста перезапустите программу");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<Share> getAvailableShares() {
        return instrumentService.getAvailableShares();
    }

    public void syncAvailableShares() {
        instrumentService.syncAvailableShares();
    }

    public List<Operation> getOperations() {
        return operationService.getAllOperations();
    }

    public List<Position> getPositions() {
        return operationService.getAllPositions();
    }

    public void addListener(EventListener eventListener) {
        operationService.addListener(eventListener);
    }
}
