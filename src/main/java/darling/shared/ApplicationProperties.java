package darling.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationProperties {

    public static final String TINKOFF_TOKEN = "t.FJso6C4iZEPRsPXcYqWdsFxBiCR-h2U7Xr7-1seVP9XhGjj04LNh93QzwiABb3Tl4SUsofVsJS7WtksCzhqaMw";

    // Первый покупки, второй продажи
    public static final List<String> ACCOUNTS = List.of("2081147399", "2089739601");
    public static final boolean SAND_MODE = true;
}
