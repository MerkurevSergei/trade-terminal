package darling.ui.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public final class PortfolioViewItem {
    private String ticker;
    private final LocalDateTime date;
    private final String direction;
    private final String price;
    private final String takeProfitPrice;
    private final String payment;
    private final long quantity;
    private final String profitPercent;
    private final String profitMoney;
}
