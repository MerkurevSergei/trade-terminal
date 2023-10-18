package darling.shared;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Utils {
    public static String accountName(String accountNumber) {
        if (buyAccountId().equals(accountNumber)) {
            return "B";
        } else if (sellAccountId().equals(accountNumber)) {
            return "S";
        }
        return "-";
    }

    public static String direction(String accountNumber, OperationType operType) {
        if (buyAccountId().equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_BUY, operType)) {
            return "B";
        } else if (sellAccountId().equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_BUY, operType)) {
            return "B!";
        } else if (sellAccountId().equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_SELL, operType)) {
            return "S";
        } else if (buyAccountId().equals(accountNumber) && Objects.equals(OperationType.OPERATION_TYPE_SELL, operType)) {
            return "S!";
        }
        return "-";
    }

    public static String buyAccountId() {
        return "2081147399";
    }

    public static String sellAccountId() {
        return "2089739601";
    }

    public static String formatLDT(LocalDateTime dateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        return dateTime == null ? "" : f.format(dateTime);
    }
}
