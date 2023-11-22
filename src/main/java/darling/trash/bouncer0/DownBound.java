package darling.trash.bouncer0;

import darling.domain.HistoricCandle;
import darling.domain.HistoricPoint;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DownBound {

    private HistoricPoint point;
    private int holdCount = 0;
    private int lessCount = 0;

    public void update(HistoricCandle lastNormalBar, Atr5 atr5) {
        if (point == null) {
            point = new HistoricPoint(lastNormalBar.time(), lastNormalBar.low(), lastNormalBar.volume());
            return;
        }

        BigDecimal delta = calcDelta(lastNormalBar, atr5);
        if (delta.compareTo(BigDecimal.ZERO) <= 0) {
            lessCount++;
            return;
        }

        if (delta.compareTo(point.price().multiply(new BigDecimal("0.0005"))) <= 0) {
            point = new HistoricPoint(point.time(), point.price().subtract(delta), lastNormalBar.volume());
            holdCount = holdCount >= 2 ? holdCount : holdCount + 1;
            lessCount = 0;
        } else {
            point = new HistoricPoint(lastNormalBar.time(), point.price().subtract(delta), lastNormalBar.volume());
            lessCount = 0;
            holdCount = 1;
        }
    }

    public BigDecimal calcDelta(HistoricCandle lastNormalBar, Atr5 atr5) {
        BigDecimal delta = point.price().subtract(lastNormalBar.low());
        BigDecimal atr07Length = atr5.atr().multiply(new BigDecimal("0.7"));
        BigDecimal atr15Length = atr5.atr().multiply(new BigDecimal("1.5"));
        BigDecimal lengthNormalBar = lastNormalBar.high().subtract(lastNormalBar.low());
        if (delta.compareTo(atr07Length) > 0 && lengthNormalBar.compareTo(atr15Length) > 0) {
            delta = delta.multiply(new BigDecimal("0.7"));
        } else if (delta.compareTo(atr07Length) > 0) {
            delta = delta.multiply(new BigDecimal("0.9"));
        }
        return delta;
    }

    public boolean isVerified() {
        return (holdCount + lessCount) >= 3;
    }

    @Override
    public String toString() {
        return point.toString();
    }
}
