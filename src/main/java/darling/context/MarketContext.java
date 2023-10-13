package darling.context;

import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.domain.Position;
import darling.repository.ShareRepository;
import darling.service.HistoryService;
import darling.service.OperationService;
import darling.service.sand.OperationSandService;
import darling.service.tinkoff.HistoryTinkoffService;
import darling.service.tinkoff.OperationTinkoffService;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MarketContext {

    public static final ShareRepository MAIN_SHARE_REPOSITORY = new ShareRepository();

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public static final HistoryService HISTORY_SERVICE = new HistoryTinkoffService();

    private final OperationService operationService;

    private final ScheduledExecutorService executorService;

    private final boolean sandMode;


    public MarketContext(boolean sandMode) {
        this.sandMode = sandMode;
        operationService = sandMode ? new OperationSandService() : new OperationTinkoffService();
        executorService = Executors.newSingleThreadScheduledExecutor();

    }

    public void start() {
        int delay = sandMode ? 0 : 2;
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
