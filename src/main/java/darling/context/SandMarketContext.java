package darling.context;

import darling.context.event.EventListener;
import darling.domain.history.HistoryService;
import darling.domain.history.TinkoffHistoryService;
import darling.repository.ShareRepository;
import ru.tinkoff.piapi.core.InvestApi;

import static darling.shared.ApplicationProperties.TINKOFF_TOKEN;

public class SandMarketContext implements MarketContext {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public static final ShareRepository MAIN_SHARE_REPOSITORY = new ShareRepository();

    public static final HistoryService HISTORY_CLIENT = new TinkoffHistoryService();

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void addOperationListener(EventListener eventListener) {

    }
}