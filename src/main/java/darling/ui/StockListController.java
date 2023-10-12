package darling.ui;

import darling.context.MarketContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class StockListController implements Initializable {

    private final InvestApi tinkoffClient = MarketContext.TINKOFF_CLIENT;

    Consumer<Share> callback;

    @FXML
    private ListView<Share> fullSharesView;

    private final List<Share> shares = new ArrayList<>();


    public void onMouseClicked(MouseEvent event) throws IOException {
        if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)) {

            Share selectedItem = fullSharesView.getSelectionModel().getSelectedItem();
            callback.accept(selectedItem);
        }
    }


    private void loadShares() {

        fullSharesView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Share t, boolean empty) {
                super.updateItem(t, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(t.getName());
                }
            }
        });

        shares.addAll(tinkoffClient.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).join());
        List<Share> sharesNames = shares.stream()
                .filter(share -> RealExchange.REAL_EXCHANGE_MOEX.equals(share.getRealExchange()))
                .sorted(Comparator.comparing(Share::getName))
                .toList();
        this.fullSharesView.setItems(FXCollections.observableArrayList(sharesNames));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadShares();
    }


    public void setCallback(Consumer<Share> consumer) {
        this.callback = consumer;
    }
}
