package darling.robot;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.HistoricCandle;
import darling.domain.LastPrice;
import darling.domain.MainShare;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static darling.context.event.Event.CLOSE_DAY;
import static darling.context.event.Event.CONTEXT_CLOSED;
import static darling.context.event.Event.CONTEXT_REFRESHED;

@RequiredArgsConstructor
public class Bouncer implements EventListener {

    private final MarketContext marketContext;
    private final MainShare mainShare;
    private final boolean sandMode;

    private HistoricCandle lastCandle;
    private HistoricCandle maxCandle;
    private HistoricCandle minCandle;
    private LastPrice lastPrice;
    private BigDecimal atr5;


    @Override
    public void handle(Event event) {
        if (CLOSE_DAY.equals(event)) closeDay();
        if (CONTEXT_CLOSED.equals(event)) turnOff();
        if (!CONTEXT_REFRESHED.equals(event)) return;
        initStep();

        updateMaxMin();
    }


    // ====================== ФУНКЦИИ ОСНОВНОГО ЦИКЛА ====================== //


    private void updateMaxMin() {
        LocalDateTime now = lastPrice.time();
        if (lastCandle == null) {
            HistoricCandle lastCandle = marketLastCandle(now);
            lastCandle = lastCandle;
            minCandle = lastCandle;
            maxCandle = lastCandle;
            return;
        }
        if (ChronoUnit.MINUTES.between(lastCandle.time(), now) > 5) {
            lastCandle = marketLastCandle(now);
        }

        if (minCandle.low().compareTo(lastCandle.low()) > 0) {
            minCandle = lastCandle;
        }
        if (maxCandle.high().compareTo(lastCandle.high()) < 0) {
            maxCandle = lastCandle;
        }
    }

    private HistoricCandle marketLastCandle(LocalDateTime now) {
        return marketContext.getCandles5Min(mainShare.uid(), now.minusMinutes(5), now)
                .stream()
                .filter(HistoricCandle::isComplete)
                .min((o1, o2) -> o2.time().compareTo(o1.time()))
                .orElse(null);
    }

    private void initStep() {
//        Optional<LastPrice> optLastPrices = marketContext.getLastPrice(mainShare.uid());
//        optLastPrices.ifPresent(price -> lastPrice = price);
//        LocalDate currentDay = lastPrice.time().toLocalDate();
//        LocalDateTime now = lastPrice.time().toLocalDate().atStartOfDay();
//        now
//        marketContext.getCandles5Min()
    }

    /**
     * Закрытие дня.
     */
    private void closeDay() {
        lastCandle = null;
        maxCandle = null;
        minCandle = null;
        lastPrice = null;
        atr5 = null;
    }


    /**
     * Действия при закрытии контекста.
     */
    private void turnOff() {
        System.out.println(minCandle);
        System.out.println(maxCandle);
    }

}