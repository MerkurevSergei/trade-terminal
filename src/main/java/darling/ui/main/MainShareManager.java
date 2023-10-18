package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.LastPrice;
import darling.ui.view.MainShareView;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static darling.context.event.Event.CONTEXT_REFRESHED;
import static darling.context.event.Event.MAIN_SHARES_UPDATED;
import static java.math.RoundingMode.HALF_UP;

public record MainShareManager(TableView<MainShareView> mainSharesTableView,
                               MarketContext marketContext) implements EventListener {

    public MainShareManager {
        TableColumn<MainShareView, String> tableColumnTiker = (TableColumn<MainShareView, String>) mainSharesTableView.getColumns().get(0);
        TableColumn<MainShareView, String> tableColumnName = (TableColumn<MainShareView, String>) mainSharesTableView.getColumns().get(1);
        TableColumn<MainShareView, String> tableColumnLot = (TableColumn<MainShareView, String>) mainSharesTableView.getColumns().get(2);
        TableColumn<MainShareView, String> tableColumnLotPrice = (TableColumn<MainShareView, String>) mainSharesTableView.getColumns().get(3);
        tableColumnTiker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().share().ticker()));
        tableColumnName.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().share().name()));
        tableColumnLot.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().share().lot().toString()));
        tableColumnLotPrice.setCellValueFactory(p -> {
            LastPrice lastPrice = p.getValue().lastPrice();
            BigDecimal lot = BigDecimal.valueOf(p.getValue().share().lot());
            String price = lastPrice != null ? lastPrice.price().multiply(lot).setScale(2, HALF_UP).toString() : "";
            return new ReadOnlyStringWrapper(price);
        });
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, MAIN_SHARES_UPDATED) && !Objects.equals(event, CONTEXT_REFRESHED)) {
            return;
        }
        Map<String, LastPrice> lastPriceDict = marketContext.getLastPrices()
                .stream()
                .collect(Collectors.toMap(LastPrice::instrumentUid, Function.identity()));
        List<MainShareView> mainShareViewList = marketContext.getMainShares()
                .stream()
                .map(share -> new MainShareView(share, lastPriceDict.get(share.uid())))
                .toList();
        mainSharesTableView.setItems(FXCollections.observableArrayList(mainShareViewList));
    }

    public void deleteMainShare() {
        marketContext.deleteMainShare(mainSharesTableView.getSelectionModel().getSelectedItem().share());
    }
}