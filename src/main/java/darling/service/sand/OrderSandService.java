package darling.service.sand;

import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.domain.Operation;
import darling.domain.order.Order;
import darling.repository.OperationRepository;
import darling.service.InstrumentService;
import darling.service.LastPriceService;
import darling.service.OrderService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

@RequiredArgsConstructor
public class OrderSandService implements OrderService {

    private final OperationRepository operationRepository;

    private final InstrumentService instrumentService;

    private final LastPriceService lastPriceService;

    private MainShare mainShare;

    @Override
    public void postOrder(String instrumentId, long quantity, BigDecimal price, ru.tinkoff.piapi.contract.v1.OrderDirection direction, String accountId, ru.tinkoff.piapi.contract.v1.OrderType type) {
        LastPrice lastPrice = lastPriceService.getLastPrice(instrumentId).orElseThrow();
        price = type.equals(OrderType.ORDER_TYPE_MARKET) ? lastPrice.price() : price;
        OperationType operationType = direction.equals(OrderDirection.ORDER_DIRECTION_BUY) ? OPERATION_TYPE_BUY : OPERATION_TYPE_SELL;

        if (mainShare == null) {
            mainShare = instrumentService.getMainShares().stream()
                    .filter(s -> s.uid().equals(instrumentId))
                    .findAny().orElseThrow();
        }
        long quantityPieces = quantity * mainShare.lot();
        Operation operation = Operation.builder()
                .id(UUID.randomUUID().toString())
                .brokerAccountId(accountId)
                .date(lastPrice.time())
                .type(operationType)
                .description(direction + " " + quantityPieces)
                .instrumentUid(instrumentId)
                .payment(price.multiply(BigDecimal.valueOf(quantityPieces)))
                .price(price)
                .commission(price.multiply(BigDecimal.valueOf(quantityPieces)).multiply(new BigDecimal("0.0005")).abs().negate())
                .quantity(quantityPieces)
                .quantityRest(0)
                .quantityDone(quantityPieces)
                .build();
        operationRepository.saveNew(List.of(operation));
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