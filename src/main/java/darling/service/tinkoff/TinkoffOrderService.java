package darling.service.tinkoff;

import darling.context.MarketContext;
import darling.domain.order.model.OrderDirection;
import darling.service.OrderService;
import ru.tinkoff.piapi.core.OrdersService;
import darling.domain.order.model.OrderType;

import java.math.BigDecimal;

public class TinkoffOrderService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

        OrdersService tinkoffOrdersService = MarketContext.TINKOFF_CLIENT.getOrdersService();
//        tinkoffOrdersService.postOrder(instrumentId, quantity, price, direction,
//                                       ApplicationProperties.ACCOUNT_ID, type, UUID.randomUUID());

    }
}