package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Position;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;

public record PositionsManager(TableView<Position> positionTableView,
                               MarketContext marketContext) implements EventListener {

    public PositionsManager {
        TableColumn<Position, String> tableColumnDate = (TableColumn<Position, String>) positionTableView.getColumns().get(0);
        TableColumn<Position, String> tableColumnOperation = (TableColumn<Position, String>) positionTableView.getColumns().get(1);
        tableColumnDate.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().instrumentUid()));
        tableColumnOperation.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().balance().toString()));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, Event.POSITION_UPDATED)) {
            return;
        }
        positionTableView.setItems(FXCollections.observableArrayList(marketContext.getPositions()));
    }
}