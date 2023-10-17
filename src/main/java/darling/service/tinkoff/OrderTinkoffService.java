package darling.service.tinkoff;

import darling.service.OrderService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static darling.shared.ApplicationProperties.ACCOUNTS;

@RequiredArgsConstructor
public class OrderTinkoffService implements OrderService {

    private final OrdersService ordersService;

    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, OrderDirection direction,
                          String accountId, OrderType type) {
        Quotation quotation = Quotation.newBuilder()
                .setUnits(price != null ? price.longValue() : 0)
                .setNano(price != null ? price.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(1_000_000_000)).intValue() : 0)
                .build();
        ordersService.postOrderSync(instrumentId, quantity, quotation, direction,
                                    accountId, type, UUID.randomUUID().toString());
    }

    @Override
    public List<OrderState> getActiveOrders(String instrumentUid) {
        return ACCOUNTS.stream()
                .map(ordersService::getOrdersSync)
                .flatMap(Collection::stream)
                .filter(order -> order.getInstrumentUid().equals(instrumentUid))
                .toList();
    }
}
