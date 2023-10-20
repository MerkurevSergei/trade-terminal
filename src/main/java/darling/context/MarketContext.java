package darling.context;

import darling.context.event.Event;
import darling.context.event.EventSubscriber;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.domain.Position;
import darling.domain.Share;
import darling.domain.order.Order;
import darling.repository.LastPriceRepository;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.MarketDataService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import darling.service.common.PortfolioCommonService;
import darling.service.sand.MarketDataSandService;
import darling.service.sand.OperationSandService;
import darling.service.sand.OrderSandService;
import darling.service.tinkoff.HistoryTinkoffService;
import darling.service.tinkoff.InstrumentTinkoffService;
import darling.service.tinkoff.MarketDataTinkoffService;
import darling.service.tinkoff.OperationTinkoffService;
import darling.service.tinkoff.OrderTinkoffService;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class MarketContext extends EventSubscriber {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    private final InstrumentService instrumentService;
    private final OperationService operationService;
    private final OrderService orderService;
    private final MarketDataService marketDataService;
    private final PortfolioService portfolioService;
    private final ScheduledExecutorService executorService;

    public static final HistoryService HISTORY_SERVICE = new HistoryTinkoffService();

    private final boolean sandMode;

    public MarketContext(boolean sandMode) {
        this.sandMode = sandMode;

        LastPriceRepository lastPriceRepository = new LastPriceRepository(new ArrayList<>());

        this.operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(TINKOFF_CLIENT.getOperationsService());
        this.instrumentService = new InstrumentTinkoffService(TINKOFF_CLIENT.getInstrumentsService());
        this.portfolioService = new PortfolioCommonService();
        this.orderService = sandMode ? new OrderSandService() : new OrderTinkoffService(instrumentService, TINKOFF_CLIENT.getOrdersService());
        this.marketDataService = sandMode ? new MarketDataSandService() : new MarketDataTinkoffService(lastPriceRepository, TINKOFF_CLIENT.getMarketDataService());
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        int delay = sandMode ? 10000 : 2500;
        notify(Event.CONTEXT_REFRESHED);
        executorService.scheduleWithFixedDelay(() -> {
            try {
                syncOperations();
                syncLastPrices(instrumentService.getMainShares());
                // syncPositions(); - итоговые позиции, понадобятся для сверки портфеля
                refreshPortfolio();
                notifyContextRefreshed();
            } catch (Exception e) {
                e.printStackTrace();
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

    public void addMainShare(MainShare share) {
        instrumentService.addMainShare(share);
        notify(Event.MAIN_SHARES_UPDATED);
    }

    public List<MainShare> getMainShares() {
        return instrumentService.getMainShares();
    }

    public void deleteMainShare(MainShare share) {
        instrumentService.deleteMainShare(share);
        notify(Event.MAIN_SHARES_UPDATED);
    }

    // ===================================================================== //
    // ======================== ПОРТФЕЛЬ И ПОЗИЦИИ ========================= //
    // ===================================================================== //

    public Portfolio getPortfolio() {
        return portfolioService.getPortfolio();
    }

    public void savePortfolio(Portfolio portfolio) {
        portfolioService.savePortfolio(portfolio);
        notify(Event.PORTFOLIO_REFRESHED);
    }

    public List<Deal> getClosedDeals() {
        return portfolioService.getClosedDeals();
    }

    private void refreshPortfolio() {
        boolean hasClosedDeals = portfolioService.refreshPortfolio();
        notify(Event.PORTFOLIO_REFRESHED);
        if (hasClosedDeals) notify(Event.CLOSED_DEALS_UPDATED);
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

    public List<Order> getActiveOrders() {
        return orderService.getActiveOrders();
    }

    public List<Order> getActiveOrders(String instrumentUid) {
        return orderService.getActiveOrders(instrumentUid);
    }

    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction,
                          String accountId, OrderType type) {
        orderService.postOrder(instrumentId, quantity, price, direction, accountId, type);
        notify(Event.ORDER_POSTED);
    }

    // ===================================================================== //
    // ============================= КОТИРОВКИ ============================= //
    // ===================================================================== //

    private void syncLastPrices(List<MainShare> shares) {
        marketDataService.syncLastPrices(shares);
    }

    public List<LastPrice> getLastPrices() {
        return marketDataService.getLastPrices();
    }

    public Optional<LastPrice> getLastPrice(String instrumentUid) {
        return marketDataService.getLastPrice(instrumentUid);
    }

    public void cancelOrder(String orderId, String accountId) {
        orderService.cancelOrder(orderId, accountId);
    }
}