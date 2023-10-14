package darling.ui.availablestock;

import darling.context.MarketContext;
import darling.domain.Share;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static java.lang.Boolean.TRUE;

public record AvailableStockManager(TableView<Share> availableShareTableView, MarketContext marketContext) {

    public AvailableStockManager {
        TableColumn<Share, String> tableColumnTiker = (TableColumn<Share, String>) availableShareTableView.getColumns().get(0);
        TableColumn<Share, String> tableColumnName = (TableColumn<Share, String>) availableShareTableView.getColumns().get(1);
        TableColumn<Share, String> tableColumnLot = (TableColumn<Share, String>) availableShareTableView.getColumns().get(2);
        TableColumn<Share, String> tableColumnPriceByLot = (TableColumn<Share, String>) availableShareTableView.getColumns().get(3);
        TableColumn<Share, String> tableColumnCurrency = (TableColumn<Share, String>) availableShareTableView.getColumns().get(4);
        TableColumn<Share, String> tableColumnShort = (TableColumn<Share, String>) availableShareTableView.getColumns().get(5);
        TableColumn<Share, String> tableColumnCountry = (TableColumn<Share, String>) availableShareTableView.getColumns().get(6);
        TableColumn<Share, String> tableColumnSector = (TableColumn<Share, String>) availableShareTableView.getColumns().get(7);
        TableColumn<Share, String> tableColumnExchange = (TableColumn<Share, String>) availableShareTableView.getColumns().get(8);
        TableColumn<Share, String> tableColumnShareType = (TableColumn<Share, String>) availableShareTableView.getColumns().get(9);
        tableColumnTiker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().ticker()));
        tableColumnName.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().name()));
        tableColumnLot.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().lot().toString()));
        tableColumnPriceByLot.setCellValueFactory(p -> new ReadOnlyStringWrapper(""));
        tableColumnCurrency.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().currency()));
        tableColumnShort.setCellValueFactory(p -> new ReadOnlyStringWrapper(TRUE.equals(p.getValue().shortEnabledFlag()) ? "Y" : "N"));
        tableColumnCountry.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().countryOfRisk()));
        tableColumnSector.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().sector()));
        tableColumnExchange.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().exchange()));
        tableColumnShareType.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().shareType()));
    }

    public void refresh() {
        availableShareTableView.setItems(FXCollections.observableArrayList(marketContext.getAvailableShares()));
    }

    public void addToMainShares() {
        marketContext.addMainShare(availableShareTableView.getSelectionModel().getSelectedItem());
    }
}