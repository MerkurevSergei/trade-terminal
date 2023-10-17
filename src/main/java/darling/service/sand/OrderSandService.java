package darling.service.sand;

import darling.domain.order.OrderDirection;
import darling.domain.order.OrderType;
import darling.service.OrderService;
import ru.tinkoff.piapi.contract.v1.OrderState;

import java.math.BigDecimal;
import java.util.List;

public class OrderSandService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

    }

    @Override
    public List<OrderState> getActiveOrders(String instrumentUid) {
        return List.of();
    }
}
