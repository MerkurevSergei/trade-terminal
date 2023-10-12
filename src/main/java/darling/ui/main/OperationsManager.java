package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.EventListener;
import darling.domain.positions.model.Operation;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public record OperationsManager(TableView<Operation> operationsTableView,
                                MarketContext marketContext) implements EventListener {

    public OperationsManager {
        operationsTableView.getColumns().clear();
        TableColumn<Operation, String> tableColumn = new TableColumn<>("Событие");
        TableColumn<Operation, String> tableColumn2 = new TableColumn<>("Сумма");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().description()));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().price().toString()));
        operationsTableView.getColumns().add(tableColumn);
        operationsTableView.getColumns().add(tableColumn2);

        marketContext.addOperationListener(this);
    }

    @Override
    public void handle(Object event) {
        List<Operation> operations = (List<Operation>) event;
        operationsTableView.setItems(FXCollections.observableArrayList(operations));
    }
}
