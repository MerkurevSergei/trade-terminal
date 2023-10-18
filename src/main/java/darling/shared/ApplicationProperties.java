package darling.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationProperties {

    //public static final String TINKOFF_TOKEN = "t.FJso6C4iZEPRsPXcYqWdsFxBiCR-h2U7Xr7-1seVP9XhGjj04LNh93QzwiABb3Tl4SUsofVsJS7WtksCzhqaMw";
    public static final String TINKOFF_TOKEN = "t.o9D4mxnwV4QBm-fhmxOl9gACPfEyIwFMe9AmV3Gk_XpALtBujc2QDwfYqhs9BXYGLhBZRpD33eD13DMImBf7TA"; // FULL

    public static final List<String> ACCOUNTS = List.of(Utils.buyAccountId(), Utils.sellAccountId());
    public static final boolean SAND_MODE = true;

    // BALANCER
    public static final boolean TRADE_ON = true;
    public static final BigDecimal PERCENT_DELTA_PROFIT_TRIGGER = new BigDecimal("0.2");
    public static final BigDecimal PERCENT_DELTA_PROFIT = new BigDecimal("0.2");
}