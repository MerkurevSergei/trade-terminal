package darling.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public final class PortfolioViewItem {
    private String ticker;
    private final String date;
    private final String direction;
    private final String price;
    private final String payment;
    private final long quantity;
}
