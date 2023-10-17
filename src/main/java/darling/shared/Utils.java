package darling.shared;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String accountName(String accountNumber) {
        if (buyAccountId().equals(accountNumber)) {
            return "B";
        } else if (sellAccountId().equals(accountNumber)) {
            return "S";
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
