package darling.context;

import darling.repository.DealRepository;
import darling.repository.OperationRepository;
import darling.repository.db.AvailableShareDbRepository;
import darling.repository.db.DealDbRepository;
import darling.repository.db.OperationDbRepository;
import darling.repository.memory.DealMemoryRepository;
import darling.repository.memory.LastPriceMemoryRepository;
import darling.repository.memory.OperationMemoryRepository;
import darling.repository.memory.PositionMemoryRepository;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.LastPriceService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import darling.service.common.HistoryTinkoffService;
import darling.service.common.LastPriceTinkoffService;
import darling.service.live.InstrumentTinkoffService;
import darling.service.live.OperationTinkoffService;
import darling.service.live.OrderTinkoffService;
import darling.service.live.PortfolioLiveService;
import darling.service.sand.OperationSandService;
import darling.service.sand.OrderSandService;
import darling.service.sand.PortfolioSandService;
import lombok.Getter;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;

import java.util.ArrayList;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;

@Getter
public final class BeanFactory {
    private final HistoryService historyService;
    private final InstrumentService instrumentService;
    private final OperationService operationService;
    private final OrderService orderService;
    private final PortfolioService portfolioService;
    private final LastPriceService lastPriceService;

    private static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public BeanFactory(boolean sandMode) {
        InstrumentsService instrumentsService = TINKOFF_CLIENT.getInstrumentsService();
        MarketDataService marketDataGrpcTinkoffService = TINKOFF_CLIENT.getMarketDataService();
        OperationsService operationsService = TINKOFF_CLIENT.getOperationsService();
        OrdersService ordersService = TINKOFF_CLIENT.getOrdersService();

        AvailableShareDbRepository availableShareDbRepository = new AvailableShareDbRepository();
        LastPriceMemoryRepository lastPriceRepository = new LastPriceMemoryRepository(new ArrayList<>());
        PositionMemoryRepository positionMemoryRepository = new PositionMemoryRepository();
        DealRepository dealRepository = sandMode ? new DealMemoryRepository() : new DealDbRepository();
        OperationRepository operationRepository = sandMode ? new OperationMemoryRepository() : new OperationDbRepository();

        this.historyService = new HistoryTinkoffService(lastPriceRepository, marketDataGrpcTinkoffService);
        this.lastPriceService = new LastPriceTinkoffService(lastPriceRepository, marketDataGrpcTinkoffService);
        this.instrumentService = new InstrumentTinkoffService(instrumentsService);
        this.operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(availableShareDbRepository, operationRepository,
                                                                                                    positionMemoryRepository, operationsService);
        this.orderService = sandMode ? new OrderSandService() : new OrderTinkoffService(this.instrumentService, ordersService);
        this.portfolioService = sandMode ? new PortfolioSandService(dealRepository) : new PortfolioLiveService(dealRepository, operationRepository);
    }
}