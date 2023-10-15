package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.shared.Utils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record OperationsManager(TableView<Operation> operationsTableView,
                                MarketContext marketContext) implements EventListener {

    public OperationsManager {
        TableColumn<Operation, String> tableColumnDate = (TableColumn<Operation, String>) operationsTableView.getColumns().get(0);
        TableColumn<Operation, String> tableColumnOperation = (TableColumn<Operation, String>) operationsTableView.getColumns().get(1);
        TableColumn<Operation, String> tableColumnPayment = (TableColumn<Operation, String>) operationsTableView.getColumns().get(2);
        TableColumn<Operation, String> tableColumnDirection = (TableColumn<Operation, String>) operationsTableView.getColumns().get(3);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        tableColumnDate.setCellValueFactory(param -> new ReadOnlyStringWrapper(f.format(param.getValue().date())));
        tableColumnOperation.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().description()));
        tableColumnPayment.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().payment().toString()));
        tableColumnDirection.setCellValueFactory(param -> new ReadOnlyStringWrapper(Utils.accountName(param.getValue().brokerAccountId())));
    }

    @Override
    public void handle(Event event) {
        boolean notOperationUpdated = !Objects.equals(event, Event.OPERATION_UPDATED);
        boolean notInit = !Objects.equals(event, Event.CONTEXT_INIT);
        if (notOperationUpdated && notInit) {
            return;
        }
        operationsTableView.setItems(FXCollections.observableArrayList(marketContext.getOperations()));
    }
}