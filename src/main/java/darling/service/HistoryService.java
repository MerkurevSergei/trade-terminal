package darling.service;

import darling.domain.HistoricPoint;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryService {

    List<HistoricCandle> getCandles(String instrumentUid, Instant from, Instant to, CandleInterval candleIntervalDay);

    List<HistoricCandle> getDailyCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getDailyPoints(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricCandle> getMinuteCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getMinutePointsByDay(String figi, LocalDateTime start, LocalDateTime end);
}