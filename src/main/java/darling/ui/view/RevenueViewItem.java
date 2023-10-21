package darling.ui.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
public final class RevenueViewItem {
    private String ticker;
    private String direction;
    private Long quantity;
    private BigDecimal revenue;
    private BigDecimal commission;
    private BigDecimal profitMoney;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;

    private DateTimeFormatter dateTimeFormatter;

    public String getDirectionView() {
        return direction == null ? "" : String.valueOf(direction);
    }

    public String getTickerView() {
        return ticker == null ? "" : String.valueOf(ticker);
    }

    public String getQuantityView() {
        return quantity == null ? "" : String.valueOf(quantity);
    }

    public String getRevenueView() {
        return revenue == null ? "" : revenue.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String getCommissionView() {
        return commission == null ? "" : commission.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String getProfitMoneyView() {
        return profitMoney == null ? "" : profitMoney.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String getOpenPriceView() {
        return openPrice == null ? "" : openPrice.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String getClosePriceView() {
        return closePrice == null ? "" : closePrice.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String getOpenDateView() {
        return openDate == null ? "" : dateTimeFormatter.format(openDate);
    }

    public String getCloseDateView() {
        return closeDate == null ? "" : dateTimeFormatter.format(closeDate);
    }
}
