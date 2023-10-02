package stock.controller;

import com.google.protobuf.Timestamp;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static stock.shared.ApplicationProperties.TINKOFF_TOKEN;

@RequiredArgsConstructor
public class StoksListFXMLController implements Initializable {

    @FXML
    private ListView<String> fullSharesView;

    private final List<Share> shares = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var api = InvestApi.create(TINKOFF_TOKEN);
        loadShares(api);
        loadHistory(api);
    }

    private void loadShares(InvestApi api) {
        shares.addAll(api.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).join());
        List<String> sharesNames = shares.stream()
                .filter(share -> RealExchange.REAL_EXCHANGE_MOEX.equals(share.getRealExchange()))
                .map(Share::getName)
                .sorted()
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
            System.out.println(candle.getTime());
        }

    }

    public void onMouseClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)) {
            //mainSharesView.getItems().add("Hello");
        }

    }
}
