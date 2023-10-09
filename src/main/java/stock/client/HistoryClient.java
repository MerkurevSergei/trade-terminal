package stock.client;

import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import stock.domain.model.HistoricPoint;

import java.time.LocalDate;
import java.util.List;

public interface HistoryClient {
    List<HistoricCandle> getDailyCandles(String figi, LocalDate start, LocalDate end);
    List<HistoricPoint> getDailyPoints(String figi, LocalDate start, LocalDate end);
}
