package darling.service.live;

import darling.domain.Share;
import darling.domain.order.Order;
import darling.mapper.TinkoffSpecialTypeMapper;
import darling.service.InstrumentService;
import darling.service.OrderService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static darling.shared.ApplicationProperties.ACCOUNTS;

@RequiredArgsConstructor
public class OrderTinkoffService implements OrderService {

    private final InstrumentService instrumentService;

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
    public List<Order> getActiveOrders() {
        return getActiveOrders(null);
    }

    @Override
    public List<Order> getActiveOrders(String instrumentUid) {
        Map<String, Share> sharesDict = instrumentService.getAvailableSharesDict();
        List<Order> activeOrders = new ArrayList<>();
        for (String accountId : ACCOUNTS) {
            List<Order> activeOrdersByAccount = ordersService.getOrdersSync(accountId)
                    .stream()
                    .filter(order -> instrumentUid == null || order.getInstrumentUid().equals(instrumentUid))
                    .map(it -> createOrder(it, sharesDict, accountId))
                    .toList();
            activeOrders.addAll(activeOrdersByAccount);
        }
        return activeOrders;
    }

    @Override
    public void cancelOrder(String orderId, String accountId) {
        ordersService.cancelOrder(accountId, orderId);
    }

    private Order createOrder(OrderState os, Map<String, Share> sharesDict, String accountId) {
        LocalDateTime date = TinkoffSpecialTypeMapper.map(os.getOrderDate());
        Share share = sharesDict.get(os.getInstrumentUid());
        return new Order(os.getOrderId(), accountId, date, share, os.getExecutionReportStatus(), os.getLotsRequested(),
                         os.getLotsExecuted());
    }
}
