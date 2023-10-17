package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Share;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;

import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.context.event.Event.MAIN_SHARES_UPDATED;

public record MainShareManager(TableView<Share> mainSharesTableView,
                               MarketContext marketContext) implements EventListener {

    public MainShareManager {
        TableColumn<Share, String> tableColumnTiker = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(0);
        TableColumn<Share, String> tableColumnName = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(1);
        TableColumn<Share, String> tableColumnLot = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(2);
        tableColumnTiker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().ticker()));
        tableColumnName.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().name()));
        tableColumnLot.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().lot().toString()));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, MAIN_SHARES_UPDATED) && !Objects.equals(event, CONTEXT_REFRESHED)) {
            return;
        }
        mainSharesTableView.setItems(FXCollections.observableArrayList(marketContext.getMainShares()));
    }

    public void deleteMainShare() {
        marketContext.deleteMainShare(mainSharesTableView.getSelectionModel().getSelectedItem());
    }
}
