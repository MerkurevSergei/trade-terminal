package stocks.domain.order;

import stocks.domain.order.model.OrderDirection;
import stocks.domain.order.model.OrderType;

import java.math.BigDecimal;

public class SandOrderService implements OrderService {
    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type) {

    }
}
