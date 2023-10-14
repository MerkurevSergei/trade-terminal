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
        TableColumn<Position, String> tableColumnTicker = (TableColumn<Position, String>) positionTableView.getColumns().get(0);
        TableColumn<Position, String> tableColumnName = (TableColumn<Position, String>) positionTableView.getColumns().get(1);
        TableColumn<Position, String> tableColumnBalance = (TableColumn<Position, String>) positionTableView.getColumns().get(2);
        TableColumn<Position, String> tableColumnLotBalance = (TableColumn<Position, String>) positionTableView.getColumns().get(3);
        TableColumn<Position, String> tableColumnAmount = (TableColumn<Position, String>) positionTableView.getColumns().get(4);
        tableColumnTicker.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().ticker()));
        tableColumnName.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().name()));
        tableColumnBalance.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().balance().toString()));
        tableColumnLotBalance.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().lotBalance().toString()));
        tableColumnAmount.setCellValueFactory(param -> new ReadOnlyStringWrapper(""));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, Event.POSITION_UPDATED)) {
            return;
        }
        positionTableView.setItems(FXCollections.observableArrayList(marketContext.getPositions()));
    }
}