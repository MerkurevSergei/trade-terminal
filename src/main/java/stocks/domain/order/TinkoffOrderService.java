package stocks.domain.order;

import ru.tinkoff.piapi.core.OrdersService;
import stocks.domain.order.model.OrderDirection;
import stocks.domain.order.model.OrderType;
import stocks.shared.ApplicationProperties;
import stocks.shared.infrastructure.BeanRegister;

import java.math.BigDecimal;
import java.util.UUID;

public class TinkoffOrderService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

        OrdersService tinkoffOrdersService = BeanRegister.TINKOFF_CLIENT.getOrdersService();
//        tinkoffOrdersService.postOrder(instrumentId, quantity, price, direction,
//                                       ApplicationProperties.ACCOUNT_ID, type, UUID.randomUUID());

    }
}
