package stock.controller;

import com.google.protobuf.Timestamp;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static stock.shared.ApplicationProperties.TINKOFF_TOKEN;

public class StockListController implements Initializable {

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


    private void loadShares(InvestApi api) {
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

        shares.addAll(api.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).join());
        List<Share> sharesNames = shares.stream()
                .filter(share -> RealExchange.REAL_EXCHANGE_MOEX.equals(share.getRealExchange()))
                .sorted(Comparator.comparing(Share::getName))
                .toList();
        this.fullSharesView.setItems(FXCollections.observableArrayList(sharesNames));
    }


    private void loadHistory(InvestApi api) {
        Share share = shares.get(0);
        Timestamp first1MinCandleDate = share.getFirst1MinCandleDate();
        Instant from = Instant.ofEpochSecond(first1MinCandleDate.getSeconds() + 3600);
        Instant to = Instant.ofEpochSecond(first1MinCandleDate.getSeconds() + 7200);

        List<HistoricCandle> history = api.getMarketDataService().getCandles(
                share.getUid(), from, to, CandleInterval.CANDLE_INTERVAL_1_MIN).join();
        for (HistoricCandle candle : history) {
            // System.out.println(candle.getTime());
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var api = InvestApi.create(TINKOFF_TOKEN);
        loadShares(api);
        loadHistory(api);
    }


    public void setCallback(Consumer<Share> consumer) {
        this.callback = consumer;
    }
}
