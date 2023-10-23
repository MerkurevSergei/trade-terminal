package darling.trash;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;

public record MarketCalculator(BigDecimal buyTaxInterest, BigDecimal sellTaxInterest, BigDecimal profitTaxInterest) {

    private static final BigDecimal V_100 = BigDecimal.valueOf(100);

    private static final int DEFAULT_SCALE = 16;

    public BigDecimal netSellPrice(BigDecimal buyPrice, BigDecimal profitInterest) {
        return buyPrice.multiply(fullFraction(profitInterest));
    }

    public BigDecimal grossSellPrice(BigDecimal buyPrice, BigDecimal profitInterest) {
        BigDecimal buyTaxFraction = fraction(buyTaxInterest);
        BigDecimal sellTaxFraction = fraction(sellTaxInterest);
        BigDecimal profitTaxFraction = fraction(profitTaxInterest);
        BigDecimal sellAndProfitFraction = sellTaxFraction.multiply(profitTaxFraction);
        BigDecimal down = ONE.subtract(sellTaxFraction).subtract(profitTaxFraction).add(sellAndProfitFraction);

        BigDecimal profitFraction = fraction(profitInterest);
        BigDecimal buyAndProfitFraction = buyTaxFraction.multiply(profitTaxFraction);
        BigDecimal up = ONE.add(profitFraction).add(buyTaxFraction).subtract(profitTaxFraction)
                .subtract(buyAndProfitFraction).multiply(buyPrice);
        return up.divide(down, DEFAULT_SCALE, HALF_UP);
    }

    public BigDecimal getSellPriceByReal(BigDecimal buyPrice, BigDecimal profitInterest) {
        return BigDecimal.ZERO;
    }

    private static BigDecimal fraction(BigDecimal interest) {
        return interest.divide(V_100, DEFAULT_SCALE, HALF_UP);

    }

    private static BigDecimal fullFraction(BigDecimal interest) {
        return interest.add(V_100).divide(V_100, DEFAULT_SCALE, HALF_UP);
    }

    public static void main(String[] args) {
        BigDecimal tax005 = new BigDecimal("0.05");
        MarketCalculator marketCalculator = new MarketCalculator(tax005, tax005, new BigDecimal("13"));
        BigDecimal bigDecimal = marketCalculator.grossSellPrice(new BigDecimal("100"), new BigDecimal("1"));
        System.out.println(bigDecimal);
        System.out.println(marketCalculator.netSellPrice(new BigDecimal("100"), new BigDecimal("1")));
    }
}
