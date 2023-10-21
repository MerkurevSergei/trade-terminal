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
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.MarketDataService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class MarketContext extends EventSubscriber {

    private final ScheduledExecutorService executorService;
    private final HistoryService historyService;
    private final InstrumentService instrumentService;
    private final MarketDataService marketDataService;
    private final OperationService operationService;
    private final OrderService orderService;
    private final PortfolioService portfolioService;
    private final boolean sandMode;

    public MarketContext(boolean sandMode) {
        super(sandMode);
        this.sandMode = sandMode;
        BeanFactory beanFactory = new BeanFactory(sandMode);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.historyService = beanFactory.getHistoryService();
        this.instrumentService = beanFactory.getInstrumentService();
        this.marketDataService = beanFactory.getMarketDataService();
        this.operationService = beanFactory.getOperationService();
        this.orderService = beanFactory.getOrderService();
        this.portfolioService = beanFactory.getPortfolioService();
    }

    public void start() {
        notifyLive(Event.CONTEXT_INITIALIZED);
        if (sandMode) startSandMode();
        else startLiveMode();
    }

    private void startSandMode() {
        //while (syncLastPrices()) {
            //syncOperations();
            //refreshPortfolio();
        //}
        notifySand(Event.CONTEXT_REFRESHED);
    }

    private void startLiveMode() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                syncOperations();
                syncLastPrices();
                // syncPositions(); - итоговые позиции, понадобятся для сверки портфеля
                refreshPortfolio();
                notifyLive(Event.CONTEXT_REFRESHED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100, 2500, MILLISECONDS);
    }

    public void stop() {
        if (sandMode) return;
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
        notifyLive(Event.MAIN_SHARES_UPDATED);
    }

    public List<MainShare> getMainShares() {
        return instrumentService.getMainShares();
    }

    public void deleteMainShare(MainShare share) {
        instrumentService.deleteMainShare(share);
        notifyLive(Event.MAIN_SHARES_UPDATED);
    }

    // ===================================================================== //
    // ======================== ПОРТФЕЛЬ И ПОЗИЦИИ ========================= //
    // ===================================================================== //

    public Portfolio getPortfolio() {
        return portfolioService.getPortfolio();
    }

    public void savePortfolio(Portfolio portfolio) {
        portfolioService.savePortfolio(portfolio);
        notifyLive(Event.PORTFOLIO_REFRESHED);
    }

    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return portfolioService.getClosedDeals(start, end);
    }

    private void refreshPortfolio() {
        boolean hasClosedDeals = portfolioService.refreshPortfolio();
        notifyLive(Event.PORTFOLIO_REFRESHED);
        if (hasClosedDeals) notifyLive(Event.CLOSED_DEALS_UPDATED);
    }

    public List<Position> getPositions() {
        return operationService.getAllPositions();
    }

    public void syncPositions() {
        operationService.syncPositions();
        notifyLive(Event.POSITION_UPDATED);
    }

    // ===================================================================== //
    // ============================= ОПЕРАЦИИ ============================= //
    // ===================================================================== //

    public List<Operation> getOperations() {
        return operationService.getAllOperations();
    }

    public void syncOperations() {
        boolean haveNew = operationService.syncOperations();
        if (haveNew) notifyLive(Event.OPERATION_UPDATED);
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
        notifyLive(Event.ORDER_POSTED);
    }

    // ===================================================================== //
    // ============================= КОТИРОВКИ ============================= //
    // ===================================================================== //

    private void syncLastPrices() {
        marketDataService.syncLastPrices(instrumentService.getMainShares());
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

    public List<HistoricCandle> getDailyCandles(String instrumentUid, LocalDateTime start, LocalDateTime end) {
        return historyService.getDailyCandles(instrumentUid, start, end);
    }
}