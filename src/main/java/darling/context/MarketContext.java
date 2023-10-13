package darling.context;

import darling.context.event.EventListener;
import darling.domain.operations.model.Operation;
import darling.service.HistoryService;
import darling.service.tinkoff.TinkoffHistoryService;
import darling.service.sand.SandOperationService;
import darling.service.OperationService;
import darling.service.tinkoff.TinkoffOperationService;
import darling.repository.ShareRepository;
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

    public static final HistoryService HISTORY_SERVICE = new TinkoffHistoryService();

    private final OperationService operationService;

    private final ScheduledExecutorService executorService;


    public MarketContext(boolean sandMode) {
        operationService = sandMode ? new SandOperationService() : new TinkoffOperationService();
        int delay = sandMode ? 0 : 2;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            operationService.sync();
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
        return operationService.getAll();
    }

    public void addListener(EventListener eventListener) {
        operationService.addListener(eventListener);
    }
}
