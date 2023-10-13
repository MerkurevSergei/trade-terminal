package darling.service;

import darling.domain.order.OrderDirection;
import darling.domain.order.OrderType;

import java.math.BigDecimal;

public interface OrderService {
    void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type);
}
