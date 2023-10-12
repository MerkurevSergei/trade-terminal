package darling.domain.order;

import darling.domain.order.model.OrderDirection;
import ru.tinkoff.piapi.core.OrdersService;
import darling.domain.order.model.OrderType;
import darling.context.SandMarketContext;

import java.math.BigDecimal;

public class TinkoffOrderService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

        OrdersService tinkoffOrdersService = SandMarketContext.TINKOFF_CLIENT.getOrdersService();
//        tinkoffOrdersService.postOrder(instrumentId, quantity, price, direction,
//                                       ApplicationProperties.ACCOUNT_ID, type, UUID.randomUUID());

    }
}
