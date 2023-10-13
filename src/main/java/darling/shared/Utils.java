package darling.shared;

public class Utils {
    public static String accountName(String accountNumber) {
        if ("2081147399".equals(accountNumber)) {
            return "B";
        } else if ("2089739601".equals(accountNumber)){
            return "S";
        }
        return "-";
    }
}
