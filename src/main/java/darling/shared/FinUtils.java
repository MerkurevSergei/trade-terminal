package darling.shared;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static darling.shared.Constants.HUNDRED;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FinUtils {

    public static BigDecimal getProfitMoney(BigDecimal amount, BigDecimal newAmount, OperationType type) {
        return type.equals(OPERATION_TYPE_SELL) ? newAmount.subtract(amount).negate() : newAmount.subtract(amount);

    }

    public static BigDecimal getProfitPercent(BigDecimal amount, BigDecimal newAmount, OperationType type) {
        return getProfitMoney(amount, newAmount, type).divide(amount, 9, RoundingMode.HALF_UP).multiply(HUNDRED);
    }

    public static String getProfitMoneyFormat(BigDecimal amount, BigDecimal newAmount, OperationType type) {
        return getProfitMoney(amount, newAmount, type).setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static String getProfitPercentFormat(BigDecimal amount, BigDecimal newAmount, OperationType type) {
        return getProfitPercent(amount, newAmount, type).setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
