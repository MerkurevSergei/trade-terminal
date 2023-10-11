package stocks.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.Share;
import stocks.shared.BeanRegister;
import stocks.ui.stockdetail.VolatilityTableManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StockDetailsController implements Initializable {

    @FXML
    private TableView<List<String>> fxmlTableViewVolatility;
    private VolatilityTableManager volatilityTableManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        volatilityTableManager = new VolatilityTableManager(fxmlTableViewVolatility, BeanRegister.HISTORY_CLIENT);
    }

    public void initData(Share share) {
        volatilityTableManager.calculateVolatility(share);
    }
}
