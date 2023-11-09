package darling.service;

import darling.domain.HistoricCandle;
import darling.domain.HistoricPoint;
import darling.domain.LastPrice;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryService {

    List<HistoricCandle> getCandles(String instrumentUid, LocalDateTime start, LocalDateTime end, CandleInterval candleIntervalDay);

    List<HistoricCandle> getDailyCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getDailyPoints(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricCandle> getMinuteCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getMinutePointsByDay(String figi, LocalDateTime start, LocalDateTime end);

    void updateLastPrice(LastPrice lastPrice);
}