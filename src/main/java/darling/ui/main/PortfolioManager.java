package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Contract;
import darling.shared.Utils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;

public record PortfolioManager(TableView<Contract> portfolioTableView,
                               MarketContext marketContext) implements EventListener {

    public PortfolioManager {
        TableColumn<Contract, String> tableColumnTicker = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(0);
        TableColumn<Contract, String> tableColumnD = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(1);
        TableColumn<Contract, String> tableColumnQuantity = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(2);
        TableColumn<Contract, String> tableColumnPrice = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(3);
        TableColumn<Contract, String> tableColumnPayment = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(4);
        TableColumn<Contract, String> tableColumnDate = (TableColumn<Contract, String>) portfolioTableView.getColumns().get(7);
        tableColumnTicker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().ticker()));
        tableColumnD.setCellValueFactory(p -> new ReadOnlyStringWrapper(Utils.accountName(p.getValue().brokerAccountId())));
        tableColumnQuantity.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().quantity())));
        tableColumnPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().price().toString()));
        tableColumnPayment.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().payment().toString()));
        tableColumnDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().date().toString()));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, Event.PORTFOLIO_REFRESHED)) {
            return;
        }
        portfolioTableView.setItems(FXCollections.observableArrayList(marketContext.getPortfolio()));
    }
}