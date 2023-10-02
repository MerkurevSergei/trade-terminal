package stock.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import stock.window.StockListWindow;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@RequiredArgsConstructor
public class MainFXMLController implements Initializable {

    @FXML
    public ListView<String> mainSharesView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void openStockFullList(ActionEvent event) throws IOException {
        StockListWindow stockListWindow1 = new StockListWindow();
        stockListWindow1.start(new Stage());
    }

}
