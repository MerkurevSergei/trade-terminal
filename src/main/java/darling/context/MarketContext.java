package darling.context;

import darling.context.event.Event;
import darling.context.event.EventSubscriber;
import darling.domain.Deal;
import darling.domain.HistoricPoint;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.domain.Position;
import darling.domain.Share;
import darling.domain.order.Order;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.LastPriceService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class MarketContext extends EventSubscriber {

    private ScheduledExecutorService executorService;
    private final HistoryService historyService;
    private final InstrumentService instrumentService;
    private final OperationService operationService;
    private final OrderService orderService;
    private final PortfolioService portfolioService;
    private final boolean sandMode;
    private final LastPriceService lastPriceService;

    public MarketContext(boolean sandMode) {
        this.sandMode = sandMode;
        BeanFactory beanFactory = new BeanFactory(sandMode);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.historyService = beanFactory.getHistoryService();
        this.instrumentService = beanFactory.getInstrumentService();
        this.lastPriceService = beanFactory.getLastPriceService();
        this.operationService = beanFactory.getOperationService();
        this.orderService = beanFactory.getOrderService();
        this.portfolioService = beanFactory.getPortfolioService();
    }

    public void start() {
        notify(Event.CONTEXT_INIT);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleWithFixedDelay(() -> {
            try {
                syncOperations();
                syncLiveLastPrices();
                // syncPositions(); - итоговые позиции, понадобятся для сверки портфеля
                refreshPortfolio();
                notify(Event.CONTEXT_REFRESHED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100, 2500, MILLISECONDS);
    }

    public void rewindHistory(String instrumentUid, LocalDateTime start, LocalDateTime end) {
        if (instrumentUid == null) throw new IllegalStateException("Не выбрана акция для загрузки истории");
        if (!sandMode) return;
        stop();
        List<HistoricPoint> historicPoints = historyService.getMinutePointsByDay(instrumentUid, start, end);
        LocalDate currentDay = historicPoints.isEmpty() ? null : historicPoints.get(0).time().toLocalDate();
        for (HistoricPoint point : historicPoints) {
            if (ChronoUnit.DAYS.between(currentDay, point.time().toLocalDate()) > 0) {
                currentDay = point.time().toLocalDate();
                notify(Event.CLOSE_DAY);
            }
            historyService.updateLastPrice(new LastPrice(instrumentUid, point.price(), point.time()));
            syncOperations();
            refreshPortfolio();
            notify(Event.CONTEXT_REFRESHED);
        }
        notify(Event.CLOSE_DAY);
        syncOperations();
        refreshPortfolio();
        notify(Event.CONTEXT_INIT);
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
        if (sandMode) return;
        notify(Event.PORTFOLIO_REFRESHED);
    }

    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return portfolioService.getClosedDeals(start, end);
    }

    private void refreshPortfolio() {
        boolean hasClosedDeals = portfolioService.refreshPortfolio();
        if (sandMode) return;
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
        if (sandMode) return;
        if (haveNew) notify(Event.OPERATION_UPDATED);
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
        if (sandMode) return;
        notify(Event.ORDER_POSTED);
    }

    // ===================================================================== //
    // ============================= КОТИРОВКИ ============================= //
    // ===================================================================== //

    private void syncLiveLastPrices() {
        lastPriceService.syncLastPrices(instrumentService.getMainShares());
        notify(Event.LAST_PRICES_UPDATED);
    }

    public List<LastPrice> getLastPrices() {
        return lastPriceService.getLastPrices();
    }

    public Optional<LastPrice> getLastPrice(String instrumentUid) {
        return lastPriceService.getLastPrice(instrumentUid);
    }

    public void cancelOrder(String orderId, String accountId) {
        orderService.cancelOrder(orderId, accountId);
    }

    public List<HistoricCandle> getDailyCandles(String instrumentUid, LocalDateTime start, LocalDateTime end) {
        return historyService.getDailyCandles(instrumentUid, start, end);
    }
}