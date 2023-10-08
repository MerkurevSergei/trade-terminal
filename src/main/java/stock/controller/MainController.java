package stock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;
import stock.client.HistoryClient;
import stock.repository.MainShareRepository;
import stock.shared.BeanRegister;
import stock.shared.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class MainController implements Initializable {

    private final MainShareRepository mainShareRepository = BeanRegister.MAIN_SHARE_REPOSITORY;
    private final HistoryClient historyClient = BeanRegister.HISTORY_CLIENT;

    @FXML
    public ListView<Share> mainSharesView;

    @FXML
    public TableView<Pair<String, String>> volatilityTableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainSharesView.setCellFactory(param -> new ListCell<>() {
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
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());

    }

    public void openStockList(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/stocklist.fxml"));
        Parent stockListFxml = loader.load();
        StockListController controller = loader.getController();
        controller.setCallback(this::addToList);

        Stage childStage = new Stage();
        childStage.setTitle("Выбор акций");
        Scene scene = new Scene(stockListFxml);
        childStage.setScene(scene);
        childStage.initModality(Modality.APPLICATION_MODAL);

        childStage.show();
    }

    public void addToList(Share value) {
        mainShareRepository.save(value);
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public void deleteFromList(ActionEvent actionEvent) {
        Share selectedItem = mainSharesView.getSelectionModel().getSelectedItem();
        mainShareRepository.deleteById(selectedItem.getFigi());
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    // TABLE VIEW
    public void loadVolatility(List<HistoricCandle> historicCandles) {
        volatilityTableView.getColumns().clear();

        TableColumn<Pair<String, String>, String> tableColumn = new TableColumn<>("One");
        TableColumn<Pair<String, String>, String> tableColumn2 = new TableColumn<>("Two");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getKey()));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue()));

        volatilityTableView.getColumns().add(tableColumn);
        volatilityTableView.getColumns().add(tableColumn2);
        //volatilityTableView.getColumns().add(new TableColumn<>("Two"));

        ArrayList<Pair<String, String>> history = new ArrayList<>();
        for (HistoricCandle candle: historicCandles) {
            BigDecimal high = Utils.quotationToBigDecimal(candle.getHigh());
            BigDecimal low = Utils.quotationToBigDecimal(candle.getLow());
            String subtract = high.subtract(low).setScale(20, RoundingMode.HALF_UP).toString();
            history.add(new Pair<>(subtract, subtract));
        }
        volatilityTableView.setItems(FXCollections.observableArrayList(history));
    }

    public void loadHistory(ActionEvent event) throws IOException {
        Share selectedItem = mainSharesView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        List<HistoricCandle> historicCandles = historyClient.loadHistory(selectedItem.getFigi());
        loadVolatility(historicCandles);
    }
}
