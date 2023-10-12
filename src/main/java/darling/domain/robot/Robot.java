package darling.domain.robot;

import darling.domain.history.HistoricPoint;

public interface Robot {
    void doStep(HistoricPoint point);
}
