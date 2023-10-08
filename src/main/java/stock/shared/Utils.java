package stock.shared;

import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

public class Utils {

    public static BigDecimal quotationToBigDecimal(Quotation quotation) {
        return quotation.getUnits() == 0 && quotation.getNano() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), 9));
    }
}
