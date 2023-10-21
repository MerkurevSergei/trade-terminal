package darling.context;

import darling.repository.AvailableShareRepository;
import darling.repository.LastPriceRepository;
import darling.repository.OperationRepository;
import darling.repository.PositionRepository;
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
import lombok.Getter;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.ArrayList;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;

@Getter
public final class BeanFactory {

    private HistoryService historyService;
    private InstrumentService instrumentService;
    private OperationService operationService;
    private OrderService orderService;
    private PortfolioService portfolioService;
    private MarketDataService marketDataService;

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public BeanFactory(boolean sandMode) {
        AvailableShareRepository availableShareRepository = new AvailableShareRepository();
        LastPriceRepository lastPriceRepository = new LastPriceRepository(new ArrayList<>());
        OperationRepository operationRepository = new OperationRepository();
        PositionRepository positionRepository = new PositionRepository();

        marketDataService = sandMode ? new MarketDataSandService() : new MarketDataTinkoffService(lastPriceRepository, TINKOFF_CLIENT.getMarketDataService());
        historyService = new HistoryTinkoffService(marketDataService);
        instrumentService = new InstrumentTinkoffService(TINKOFF_CLIENT.getInstrumentsService());
        operationService = sandMode ? new OperationSandService() : new OperationTinkoffService(availableShareRepository, operationRepository, positionRepository, TINKOFF_CLIENT.getOperationsService());
        orderService = sandMode ? new OrderSandService() : new OrderTinkoffService(instrumentService, TINKOFF_CLIENT.getOrdersService());
        portfolioService = new PortfolioCommonService();
    }
}