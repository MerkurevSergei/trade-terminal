package darling.context;

import darling.repository.db.AvailableShareRepository;
import darling.repository.db.DealRepository;
import darling.repository.db.OperationRepository;
import darling.repository.memory.LastPriceMemoryRepository;
import darling.repository.memory.PositionRepository;
import darling.service.HistoryService;
import darling.service.InstrumentService;
import darling.service.LastPriceService;
import darling.service.OperationService;
import darling.service.OrderService;
import darling.service.PortfolioService;
import darling.service.common.HistoryTinkoffService;
import darling.service.common.PortfolioCommonService;
import darling.service.live.InstrumentTinkoffService;
import darling.service.common.LastPriceTinkoffService;
import darling.service.live.OperationTinkoffService;
import darling.service.live.OrderTinkoffService;
import darling.service.sand.OperationSandService;
import darling.service.sand.OrderSandService;
import lombok.Getter;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;

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

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public BeanFactory(boolean sandMode) {
        MarketDataService marketDataGrpcTinkoffService = TINKOFF_CLIENT.getMarketDataService();

        AvailableShareRepository availableShareRepository = new AvailableShareRepository();
        DealRepository dealRepository = new DealRepository();
        LastPriceMemoryRepository lastPriceRepository = new LastPriceMemoryRepository(new ArrayList<>());
        OperationRepository operationRepository = new OperationRepository();
        PositionRepository positionRepository = new PositionRepository();

        historyService = new HistoryTinkoffService(TINKOFF_CLIENT.getMarketDataService());
        lastPriceService = new LastPriceTinkoffService(lastPriceRepository, marketDataGrpcTinkoffService);
        instrumentService = new InstrumentTinkoffService(TINKOFF_CLIENT.getInstrumentsService());
        operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(availableShareRepository, operationRepository, positionRepository, TINKOFF_CLIENT.getOperationsService());
        orderService = sandMode ? new OrderSandService() : new OrderTinkoffService(instrumentService, TINKOFF_CLIENT.getOrdersService());
        portfolioService = new PortfolioCommonService(dealRepository, operationRepository);
    }
}