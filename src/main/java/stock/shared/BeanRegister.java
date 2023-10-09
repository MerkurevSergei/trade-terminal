package stock.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.tinkoff.piapi.core.InvestApi;
import stock.client.HistoryClient;
import stock.client.TinkoffApiHistoryClient;
import stock.repository.MainShareRepository;

import static stock.shared.ApplicationProperties.TINKOFF_TOKEN;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanRegister {

    public static final InvestApi TINKOFF_CLIENT = InvestApi.create(TINKOFF_TOKEN);

    public static final MainShareRepository MAIN_SHARE_REPOSITORY = new MainShareRepository();

    public static final HistoryClient HISTORY_CLIENT = new TinkoffApiHistoryClient();

}
