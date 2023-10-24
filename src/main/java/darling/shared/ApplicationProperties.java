package darling.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationProperties {

    // PERSONAL
    //public static final String TINKOFF_TOKEN = "t.FJso6C4iZEPRsPXcYqWdsFxBiCR-h2U7Xr7-1seVP9XhGjj04LNh93QzwiABb3Tl4SUsofVsJS7WtksCzhqaMw";
    public static final String TINKOFF_TOKEN = "t.o9D4mxnwV4QBm-fhmxOl9gACPfEyIwFMe9AmV3Gk_XpALtBujc2QDwfYqhs9BXYGLhBZRpD33eD13DMImBf7TA"; // FULL
    public static final String ACCOUNT_BUY = "2081147399";
    public static final String ACCOUNT_SELL = "2089739601";
    public static final List<String> ACCOUNTS = List.of(ACCOUNT_BUY, ACCOUNT_SELL);

    // BALANCER
    public static BigDecimal PERCENT_DELTA_PROFIT_TRIGGER = new BigDecimal("0.02");
    public static BigDecimal PERCENT_DELTA_PROFIT = new BigDecimal("0.22");
    public static BigDecimal PERCENT_PROFIT_CLEAR_LAG = new BigDecimal("20");
    public static final BigDecimal EMPTY_LEVEL_DELTA = PERCENT_DELTA_PROFIT.add(PERCENT_DELTA_PROFIT_TRIGGER).add(new BigDecimal("0.001"));
    public static final long NEW_REQUEST_FROZEN_SECONDS = 30;
}