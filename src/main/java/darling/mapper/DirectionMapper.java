package darling.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;

import java.util.Objects;

import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_UNSPECIFIED;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL;
import static ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_UNSPECIFIED;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectionMapper {
    public static OrderDirection map(OperationType operationType) {
        if (Objects.equals(operationType, OPERATION_TYPE_SELL)) return ORDER_DIRECTION_SELL;
        if (Objects.equals(operationType, OPERATION_TYPE_BUY)) return ORDER_DIRECTION_BUY;
        return ORDER_DIRECTION_UNSPECIFIED;
    }

    public static OperationType map(OrderDirection operationType) {
        if (Objects.equals(operationType, ORDER_DIRECTION_SELL)) return OPERATION_TYPE_SELL;
        if (Objects.equals(operationType, ORDER_DIRECTION_BUY)) return OPERATION_TYPE_BUY;
        return OPERATION_TYPE_UNSPECIFIED;
    }

    public static OrderDirection mapRevert(OperationType operationType) {
        if (Objects.equals(operationType, OPERATION_TYPE_SELL)) return ORDER_DIRECTION_BUY;
        if (Objects.equals(operationType, OPERATION_TYPE_BUY)) return ORDER_DIRECTION_SELL;
        return ORDER_DIRECTION_UNSPECIFIED;
    }

    public static OperationType mapRevert(OrderDirection operationType) {
        if (Objects.equals(operationType, ORDER_DIRECTION_SELL)) return OPERATION_TYPE_BUY;
        if (Objects.equals(operationType, ORDER_DIRECTION_BUY)) return OPERATION_TYPE_SELL;
        return OPERATION_TYPE_UNSPECIFIED;
    }
}
