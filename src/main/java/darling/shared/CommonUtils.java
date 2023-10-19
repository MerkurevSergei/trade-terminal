package darling.shared;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static darling.shared.ApplicationProperties.ACCOUNT_BUY;
import static darling.shared.ApplicationProperties.ACCOUNT_SELL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

    public static String direction(String accountNumber, OperationType operType) {
        if (ACCOUNT_BUY.equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_BUY, operType)) {
            return "B";
        } else if (ACCOUNT_SELL.equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_BUY, operType)) {
            return "B!";
        } else if (ACCOUNT_SELL.equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_SELL, operType)) {
            return "S";
        } else if (ACCOUNT_BUY.equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_SELL, operType)) {
            return "S!";
        }
        return "-";
    }

    public static String formatLDT(LocalDateTime dateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        return dateTime == null ? "" : f.format(dateTime);
    }
}
