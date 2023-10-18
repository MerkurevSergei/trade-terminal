package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.PortfolioViewItem;
import darling.shared.Utils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;

public record PortfolioManager(TableView<PortfolioViewItem> portfolioTableView,
                               MarketContext marketContext) implements EventListener {

    public PortfolioManager {
        TableColumn<PortfolioViewItem, String> tableColumnTicker = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(0);
        TableColumn<PortfolioViewItem, String> tableColumnD = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(1);
        TableColumn<PortfolioViewItem, String> tableColumnQuantity = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(2);
        TableColumn<PortfolioViewItem, String> tableColumnPrice = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(3);
        TableColumn<PortfolioViewItem, String> tableColumnProfitPrice = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(4);
        TableColumn<PortfolioViewItem, String> tableColumnPayment = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(5);
        TableColumn<PortfolioViewItem, String> tableColumnDate = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(8);
        tableColumnTicker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTicker()));
        tableColumnD.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDirection()));
        tableColumnQuantity.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getQuantity())));
        tableColumnPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getPrice()));
        tableColumnProfitPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTakeProfitPrice()));
        tableColumnPayment.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getPayment()));
        tableColumnDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(Utils.formatLDT(p.getValue().getDate())));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, Event.PORTFOLIO_REFRESHED)) {
            return;
        }
        portfolioTableView.setItems(FXCollections.observableArrayList(marketContext.getPortfolioView()));
    }
}