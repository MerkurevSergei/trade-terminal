package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Share;
import darling.domain.order.Order;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;
import java.util.Set;

import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.context.event.Event.MAIN_SHARES_UPDATED;
import static darling.context.event.Event.ORDER_POSTED;

public record ActiveOrderManager(TableView<Order> activeOrdersTableView,
                                 MarketContext marketContext) implements EventListener {

    public ActiveOrderManager {
        TableColumn<Order, String> tableColumnId = (TableColumn<Order, String>) activeOrdersTableView.getColumns().get(0);
        TableColumn<Order, String> tableColumnDate = (TableColumn<Order, String>) activeOrdersTableView.getColumns().get(1);
        TableColumn<Order, String> tableColumnStatus = (TableColumn<Order, String>) activeOrdersTableView.getColumns().get(2);
        TableColumn<Order, String> tableColumnRest = (TableColumn<Order, String>) activeOrdersTableView.getColumns().get(3);
        tableColumnId.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().orderId()));
        tableColumnDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().date().toString()));
        tableColumnStatus.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().status().toString()));
        tableColumnRest.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().lotsRest())));
    }

    @Override
    public void handle(Event event) {
        if (!Set.of(CONTEXT_REFRESHED, ORDER_POSTED).contains(event)) {
            return;
        }
        activeOrdersTableView.setItems(FXCollections.observableArrayList(marketContext.getActiveOrders()));
    }
}
