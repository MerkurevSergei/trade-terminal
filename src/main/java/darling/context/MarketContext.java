package darling.context;

import darling.context.event.EventListener;
import darling.service.HistoryService;
import darling.service.tinkoff.TinkoffHistoryService;
import darling.service.sand.SandOperationService;
import darling.service.OperationService;
import darling.service.tinkoff.TinkoffOperationService;
import darling.repository.ShareRepository;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MarketContext {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public static final ShareRepository MAIN_SHARE_REPOSITORY = new ShareRepository();

    public static final HistoryService HISTORY_CLIENT = new TinkoffHistoryService();

    private final OperationService operationService;

    private final ScheduledExecutorService executorService;


    public MarketContext(boolean sandMode) {
        operationService = sandMode ? new SandOperationService() : new TinkoffOperationService();
        int delay = sandMode ? 0 : 2;
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> operationService.refreshOperations(start, end), 1, delay, SECONDS);
    }

    public void stop() {
        executorService.shutdown();
        try {
            boolean isCompleted = executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addListener(EventListener eventListener) {
        operationService.addListener(eventListener);
    }
}
