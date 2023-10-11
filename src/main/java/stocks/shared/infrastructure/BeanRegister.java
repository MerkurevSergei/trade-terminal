package stocks.shared.infrastructure;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.tinkoff.piapi.core.InvestApi;
import stocks.repository.ShareRepository;
import stocks.domain.history.HistoryService;
import stocks.domain.history.TinkoffHistoryService;

import static stocks.shared.ApplicationProperties.TINKOFF_TOKEN;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanRegister {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public static final ShareRepository MAIN_SHARE_REPOSITORY = new ShareRepository();

    public static final HistoryService HISTORY_CLIENT = new TinkoffHistoryService();

}
