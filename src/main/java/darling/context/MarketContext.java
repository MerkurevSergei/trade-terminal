package darling.context;

import darling.context.event.Event;
import darling.context.event.EventSubscriber;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.domain.PortfolioViewItem;
import darling.domain.Position;
import darling.domain.Share;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import darling.service.common.PortfolioCommonService;
import darling.service.sand.OperationSandService;
import darling.service.sand.OrderSandService;
import darling.service.tinkoff.HistoryTinkoffService;
import darling.service.tinkoff.InstrumentTinkoffService;
import darling.service.tinkoff.OperationTinkoffService;
import darling.service.tinkoff.OrderTinkoffService;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class MarketContext extends EventSubscriber {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    private final OperationService operationService;
    private final InstrumentService instrumentService;
    private final ScheduledExecutorService executorService;
    private final PortfolioService portfolioService;
    private final OrderService orderService;

    public static final HistoryService HISTORY_SERVICE = new HistoryTinkoffService();

    private final boolean sandMode;

    public MarketContext(boolean sandMode) {
        this.sandMode = sandMode;
        this.operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(TINKOFF_CLIENT.getOperationsService());
        this.instrumentService = new InstrumentTinkoffService(TINKOFF_CLIENT.getInstrumentsService());
        this.portfolioService = new PortfolioCommonService(instrumentService);
        this.orderService = sandMode ? new OrderSandService() : new OrderTinkoffService(TINKOFF_CLIENT.getOrdersService());
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        int delay = sandMode ? 10000 : 2000;
        notify(Event.CONTEXT_REFRESHED);
        executorService.scheduleWithFixedDelay(() -> {
            try {
                syncPositions();
                syncOperations();
                refreshPortfolio();
                notifyContextRefreshed();
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }, 100, delay, MILLISECONDS);
        notify(Event.CONTEXT_STARTED);
    }

    public void stop() {
        executorService.shutdown();
        try {
            boolean isCompleted = executorService.awaitTermination(1, TimeUnit.MINUTES);
            if (!isCompleted) {
                throw new IllegalStateException("Не удалось корректно переключить режим. Пожалуйста перезапустите программу");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    // ===================================================================== //
    // ==================== ДОСТУПНЫЕ У БРОКЕРА  АКЦИИ ===================== //
    // ===================================================================== //

    public List<Share> getAvailableShares() {
        return instrumentService.getAvailableShares();
    }

    public void syncAvailableShares() {
        instrumentService.syncAvailableShares();
    }

    // ===================================================================== //
    // ============= ОТОБРАННЫЕ ПОЛЬЗОВАТЕЛЕМ ДЛЯ РАБОТЫ АКЦИЙ ============= //
    // ===================================================================== //

    public void addMainShare(Share share) {
        instrumentService.addMainShare(share);
        notify(Event.MAIN_SHARES_UPDATED);
    }

    public List<Share> getMainShares() {
        return instrumentService.getMainShares();
    }

    public void deleteMainShare(Share share) {
        instrumentService.deleteMainShare(share);
        notify(Event.MAIN_SHARES_UPDATED);
    }

    // ===================================================================== //
    // ======================== ПОРТФЕЛЬ И ПОЗИЦИИ ========================= //
    // ===================================================================== //

    public Portfolio getPortfolio() {
        return portfolioService.getPortfolio();
    }

    public List<PortfolioViewItem> getPortfolioView() {
        return portfolioService.getView();
    }

    private void refreshPortfolio() {
        portfolioService.refreshPortfolio();
        notify(Event.PORTFOLIO_REFRESHED);
    }

    public List<Position> getPositions() {
        return operationService.getAllPositions();
    }

    public void syncPositions() {
        operationService.syncPositions();
        notify(Event.POSITION_UPDATED);
    }

    // ===================================================================== //
    // ============================= ОПЕРАЦИИ ============================= //
    // ===================================================================== //

    public List<Operation> getOperations() {
        return operationService.getAllOperations();
    }

    public void syncOperations() {
        boolean haveNew = operationService.syncOperations();
        if (haveNew) notify(Event.OPERATION_UPDATED);
    }


    // ===================================================================== //
    // ============================== КОНТЕКСТ ============================= //
    // ===================================================================== //

    private void notifyContextRefreshed() {
        notify(Event.CONTEXT_REFRESHED);
    }

    // ===================================================================== //
    // =============================== ОРДЕРА ============================== //
    // ===================================================================== //

    public List<OrderState> getActiveOrders(String instrumentUid) {
        return orderService.getActiveOrders(instrumentUid);
    }

    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction,
                          String accountId, OrderType type) {
        orderService.postOrder(instrumentId, quantity, price, direction, accountId, type);
    }
}
