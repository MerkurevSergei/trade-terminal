package darling.service;

import darling.domain.order.model.OrderDirection;
import darling.domain.order.model.OrderType;

import java.math.BigDecimal;

public interface OrderService {
    void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, OrderType type);
}
