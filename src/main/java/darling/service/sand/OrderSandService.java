package darling.service.sand;

import darling.domain.order.OrderDirection;
import darling.domain.order.OrderType;
import darling.service.OrderService;

import java.math.BigDecimal;

public class OrderSandService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

    }
}
