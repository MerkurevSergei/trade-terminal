package stocks.client;

import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import stocks.domain.model.HistoricPoint;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryClient {
    List<HistoricCandle> getDailyCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getDailyPoints(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricCandle> getMinuteCandles(String figi, LocalDateTime start, LocalDateTime end);

    List<HistoricPoint> getMinutePointsByDay(String figi, LocalDateTime start, LocalDateTime end);
}
