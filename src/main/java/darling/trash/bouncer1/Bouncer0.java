package darling.trash.bouncer1;

import darling.domain.HistoricCandle;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import darling.service.HistoryService;
import darling.service.LastPriceService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.MAX;
import static ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN;

@RequiredArgsConstructor
public class Bouncer0 {

    private final HistoryService historyService;
    private final LastPriceService lastPriceService;
    private final LocalDate currentDay;
    private final LocalDate previousDay;
    private final String instrumentUid;
    private final String instrumentName;

    private int upTwice = 0;
    private int downTwice = 0;

    public String step() {

        Atr5 atr5 = new Atr5(historyService, previousDay, instrumentUid);
        List<HistoricCandle> bars = historyService.getCandles(
                instrumentUid, currentDay.atStartOfDay(), currentDay.atTime(MAX), CANDLE_INTERVAL_5_MIN);

        UpBound upBound = new UpBound();
        DownBound downBound = new DownBound();
        for (HistoricCandle lastBar : bars) {
            HistoricCandle lastNormalBar = atr5.normalize(lastBar);
            atr5.add(lastNormalBar);
            upBound.update(lastNormalBar, atr5);
            downBound.update(lastNormalBar, atr5);
        }

        StringBuilder result = new StringBuilder();
        lastPriceService.syncLastPrices(List.of(new MainShare(instrumentUid, null, null, null, null, true)));
        Optional<LastPrice> lastPrice = lastPriceService.getLastPrice(instrumentUid);
        if (lastPrice.isEmpty()) {
            return result.toString();
        }
        if (isUpTvx(upBound, lastPrice.get())) {
            result.append("=====").append(instrumentName).append("=====").append(System.lineSeparator());
            result.append("UP").append(System.lineSeparator());
            result.append(upBound);
        }
        if (isDownTvx(downBound, lastPrice.get())) {
            if (result.isEmpty()) {
                result.append("=====").append(instrumentName).append("=====").append(System.lineSeparator());
            }
            result.append("DOWN").append(System.lineSeparator());
            result.append(downBound);
        }
        return result.toString();
    }

    private boolean priceOnUpBound(UpBound upBound, BigDecimal price) {
        if (!upBound.isVerified()) return false;
        BigDecimal upPrice = upBound.getPoint().price();
        BigDecimal downPrice = upPrice.multiply(new BigDecimal("0.9990"));
        return price.compareTo(upPrice) <= 0 && price.compareTo(downPrice) >= 0;
    }

    private boolean priceOnDownBound(DownBound downBound, BigDecimal price) {
        if (!downBound.isVerified()) return false;
        BigDecimal downPrice = downBound.getPoint().price();
        BigDecimal upPrice = downPrice.multiply(new BigDecimal("1.0010"));
        return price.compareTo(upPrice) <= 0 && price.compareTo(downPrice) >= 0;
    }

    private boolean isUpTvx(UpBound upBound, LastPrice point) {
        if (priceOnUpBound(upBound, point.price())) {
            upTwice++;
            return upTwice >= 3;
        }
        upTwice = 0;
        return false;
    }

    private boolean isDownTvx(DownBound downBound, LastPrice point) {
        if (priceOnDownBound(downBound, point.price())) {
            downTwice++;
            return downTwice >= 3;
        }
        downTwice = 0;
        return false;
    }
}
