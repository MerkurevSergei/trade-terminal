package darling.trash.bouncer1;

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

    private BigDecimal tail;

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

        BigDecimal tailSum1 = candles.stream()
                .filter(c -> c.open().compareTo(c.close()) >= 0)
                .map(c -> c.high().subtract(c.open()).add(c.close().subtract(c.low())))
                .sorted()
                .skip(10)
                .sorted(Comparator.reverseOrder())
                .skip(10)
                .reduce(BigDecimal::add).orElse(ZERO);
        BigDecimal tailSum2 = candles.stream()
                .filter(c -> c.open().compareTo(c.close()) < 0)
                .map(c -> c.high().subtract(c.close()).add(c.open().subtract(c.low())))
                .sorted()
                .skip(10)
                .sorted(Comparator.reverseOrder())
                .skip(10)
                .reduce(BigDecimal::add).orElse(ZERO);

        atrCount = BigDecimal.valueOf(candles.size());

        if (atrCount.compareTo(ZERO) > 0) {
            tail = tailSum1.add(tailSum2).multiply(new BigDecimal("0.5")).divide(atrCount, 9, HALF_UP);
        } else {
            tail = ZERO;
        }
    }

    public void add(HistoricCandle bar) {
        BigDecimal length = bar.high().subtract(bar.low());
        atrSum = atrSum.add(length);
        atrCount = atrCount.add(BigDecimal.ONE);
    }

    public BigDecimal get() {
        return atrCount.compareTo(ZERO) > 0 ? atrSum.divide(atrCount, 9, HALF_UP) : ZERO;
    }


    public HistoricCandle normalize(HistoricCandle bar) {
        bar = normalizeTail(bar);
        BigDecimal length = bar.high().subtract(bar.low());
        BigDecimal center = length.divide(BigDecimal.valueOf(2), 9, HALF_UP).add(bar.low());
        if (get().multiply(new BigDecimal("2.0")).compareTo(length) < 0) {
            length = get().multiply(new BigDecimal("2.0"));
        } else if (get().multiply(new BigDecimal("0.3")).compareTo(length) > 0) {
            length = get().multiply(new BigDecimal("0.3"));
        }
        BigDecimal halfNormalLength = length.divide(BigDecimal.valueOf(2), 9, HALF_UP);
        return new HistoricCandle(bar.time(), null, null, center.subtract(halfNormalLength),
                                  center.add(halfNormalLength), bar.volume(), bar.isComplete());
    }

    public BigDecimal atrTail() {
        return tail;
    }

    private HistoricCandle normalizeTail(HistoricCandle bar) {
        BigDecimal upTail;
        BigDecimal downTail;
        if (bar.open().compareTo(bar.close()) >= 0) {
            upTail = bar.high().subtract(bar.open());
            downTail = bar.close().subtract(bar.low());
        } else {
            upTail = bar.high().subtract(bar.close());
            downTail = bar.open().subtract(bar.low());
        }
        if (atrTail().multiply(new BigDecimal("1.8")).compareTo(upTail) < 0) {
            upTail = atrTail().multiply(new BigDecimal("1.8"));
        }
        if (atrTail().multiply(new BigDecimal("1.8")).compareTo(downTail) < 0) {
            downTail = atrTail().multiply(new BigDecimal("1.8"));
        }

        if (bar.open().compareTo(bar.close()) >= 0) {
            return new HistoricCandle(bar.time(), bar.open(), bar.close(),
                                      bar.open().add(upTail), bar.close().subtract(downTail), bar.volume(), true);
        } else {
            return new HistoricCandle(bar.time(), bar.open(), bar.close(),
                                      bar.close().add(upTail), bar.open().subtract(downTail), bar.volume(), true);
        }
    }
}
