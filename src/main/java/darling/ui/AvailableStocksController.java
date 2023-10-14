package darling.ui;

import darling.context.MarketContext;
import darling.domain.Share;
import darling.ui.availablestock.AvailableStockManager;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AvailableStocksController extends AbstractController {

    @FXML
    public TableView<Share> fxmlTableViewAvailableShares;
    private AvailableStockManager availableStockManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public void setContextAndInit(MarketContext marketContext) {
        this.marketContext = marketContext;
        this.availableStockManager = new AvailableStockManager(fxmlTableViewAvailableShares, marketContext);
        this.availableStockManager.refresh();
    }

    public void onMouseClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)) {
            availableStockManager.addToMainShares();
        }
    }

    public void syncAvailableShares() {
        log.info("=== Update shares from exchange ===");
        marketContext.syncAvailableShares();
        availableStockManager.refresh();
    }
}
