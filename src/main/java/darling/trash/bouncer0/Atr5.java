package darling.trash.bouncer0;

import darling.domain.HistoricCandle;
import darling.service.HistoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalTime.MAX;
import static ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_5_MIN;

public class Atr5 {

    private final HistoryService historyService;

    private BigDecimal atrSum;

    private BigDecimal atrCount;

    public Atr5(HistoryService historyService, LocalDate date, String instrumentUid) {
        this.historyService = historyService;
        List<HistoricCandle> candles = this.historyService.getCandles(instrumentUid, date.atStartOfDay(),
                date.atTime(MAX), CANDLE_INTERVAL_5_MIN);
        atrSum = candles.stream()
                .map(c -> c.high().subtract(c.low()))
                .sorted()
                .skip(10)
                .sorted(Comparator.reverseOrder())
                .skip(10)
                .reduce(BigDecimal::add).orElse(ZERO);
        atrCount = BigDecimal.valueOf(candles.size());
    }

    public void add(BigDecimal barLength) {
        if (!isStandardLength(barLength)) return;
        atrSum = atrSum.add(barLength);
        atrCount = atrCount.add(BigDecimal.ONE);
    }

    public BigDecimal atr5() {
        return atrCount.compareTo(ZERO) > 0 ? atrSum.divide(atrCount, 9, HALF_UP) : ZERO;
    }

    public boolean isStandardLength(BigDecimal barLength) {
        if (atr5().multiply(new BigDecimal("1.8")).compareTo(barLength) < 0) return false;
        if (atr5().multiply(new BigDecimal("0.3")).compareTo(barLength) > 0) return false;
        return true;
    }
}
