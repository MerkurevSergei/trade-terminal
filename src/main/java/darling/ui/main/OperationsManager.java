package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.shared.CommonUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Set;

import static darling.context.event.Event.CONTEXT_INIT;
import static darling.context.event.Event.OPERATION_UPDATED;

public record OperationsManager(TableView<Operation> operationsTableView,
                                MarketContext marketContext) implements EventListener {

    public OperationsManager {
        TableColumn<Operation, String> tableColumnDate = (TableColumn<Operation, String>) operationsTableView.getColumns().get(0);
        TableColumn<Operation, String> tableColumnOperation = (TableColumn<Operation, String>) operationsTableView.getColumns().get(1);
        TableColumn<Operation, String> tableColumnPayment = (TableColumn<Operation, String>) operationsTableView.getColumns().get(2);
        TableColumn<Operation, String> tableColumnDirection = (TableColumn<Operation, String>) operationsTableView.getColumns().get(3);
        tableColumnDate.setCellValueFactory(param -> new ReadOnlyStringWrapper(CommonUtils.formatLDT(param.getValue().date())));
        tableColumnOperation.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().description()));
        tableColumnPayment.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().payment().toString()));
        tableColumnDirection.setCellValueFactory(param -> new ReadOnlyStringWrapper(CommonUtils.direction(param.getValue().brokerAccountId(), param.getValue().type())));
    }

    @Override
    public void handle(Event event) {
        if (!Set.of(OPERATION_UPDATED, CONTEXT_INIT).contains(event)) {
            return;
        }
        operationsTableView.setItems(FXCollections.observableArrayList(marketContext.getOperations()));
    }
}