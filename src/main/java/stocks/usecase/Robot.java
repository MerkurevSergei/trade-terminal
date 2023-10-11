package stocks.usecase;

import stocks.domain.history.HistoricPoint;

public interface Robot {
    void doStep(HistoricPoint point);
}
