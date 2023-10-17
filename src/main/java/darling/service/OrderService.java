package darling.service;

import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {


    void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction, String accountId,
                   OrderType type);

    List<OrderState> getActiveOrders(String instrumentUid);
}
