package darling.service.sand;

import darling.domain.order.Order;
import darling.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

public class OrderSandService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, ru.tinkoff.piapi.contract.v1.OrderDirection direction, String accountId, ru.tinkoff.piapi.contract.v1.OrderType type) {

    }

    @Override
    public List<Order> getActiveOrders() {
        return List.of();
    }

    @Override
    public List<Order> getActiveOrders(String instrumentUid) {
        return List.of();
    }

    @Override
    public void cancelOrder(String orderId, String accountId) {

    }
}
