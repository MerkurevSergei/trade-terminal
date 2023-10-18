package darling.shared;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
}
