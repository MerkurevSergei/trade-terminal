package stock.controller;

import com.google.protobuf.Timestamp;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static stock.shared.ApplicationProperties.TINKOFF_TOKEN;

public class StockListController implements Initializable {

    Consumer<String> callback;
//    private StockListWindow(Application aThis) {
//        parent = aThis;
//
//        subStage = new Stage();
//        Group subRoot = new Group();
//        Scene scene = new Scene(subRoot, 300, 200);
//        subStage.setScene(scene);
//        subStage.show();
//
//        VBox vBox = new VBox();
//
//        labelID = new Label();
//        labelID.setText(subStage.toString());
//
//        messageIn = new Label();
//        subTextField = new TextField();
//
//        subSendButton = new Button("Send to main Window");
//        subSendButton.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                setMainMsg(subTextField.getText());
//            }
//
//        });
//
//        vBox.getChildren().addAll(labelID, messageIn, subTextField, subSendButton);
//        subRoot.getChildren().add(vBox);
//    }
//
//    public void start(Stage stage) throws IOException {
//        FXMLLoader loader = new FXMLLoader();
//        URL xmlUrl = getClass().getResource("/stocklist.fxml");
//        loader.setLocation(xmlUrl);
//        Parent root = loader.load();
//
//        stage.setTitle(new String("Моя прелесть".getBytes("WINDOWS-1251"), StandardCharsets.UTF_8));
//        stage.setScene(new Scene(root));
//        stage.show();
//    }

    @FXML
    private ListView<String> fullSharesView;

    private final List<Share> shares = new ArrayList<>();


    public void onMouseClicked(MouseEvent event) throws IOException {
        if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)) {
            callback.accept("Hello");
        }
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
            // System.out.println(candle.getTime());
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var api = InvestApi.create(TINKOFF_TOKEN);
        loadShares(api);
        loadHistory(api);
    }


    public void setCallback(Consumer<String> consumer) {
        this.callback = consumer;
    }
}
