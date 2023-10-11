package stocks.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.Share;
import stocks.shared.BeanRegister;
import stocks.shared.JavaFxUtils;
import stocks.ui.trade.RevenueTableManager;
import stocks.ui.trade.ShareListManager;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

@Getter
public class MainTradeController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.shareListManager = new ShareListManager(fxmlListViewShare, BeanRegister.MAIN_SHARE_REPOSITORY);
        this.revenueTableManager = new RevenueTableManager(fxmlTableViewRevenue, BeanRegister.HISTORY_CLIENT);
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, exception) -> {
                    exception.printStackTrace();
                    Platform.runLater(() -> showError(getRootCause(exception).getMessage()));
                }
        );
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause != null) ? getRootCause(cause) : throwable;
    }

    // ===================================================================== //
    // =========================== СПИСОК АКЦИЙ ============================ //
    // ===================================================================== //

    @FXML
    private ListView<Share> fxmlListViewShare;
    private ShareListManager shareListManager;

    public void addShare(Share share) {
        shareListManager.addShare(share);
    }

    public void deleteShare() {
        shareListManager.deleteActiveShare();
    }

    // ===================================================================== //
    // ======================= ТАБЛИЦА ДОХОДНОСТИ ======================= //
    // ===================================================================== //

    @FXML
    private TableView<Map.Entry<String, BigDecimal>> fxmlTableViewRevenue;
    private RevenueTableManager revenueTableManager;

    public void calculateRevenue() {
        revenueTableManager.calculateRevenue(shareListManager.getSelectedItem());
    }


    // ===================================================================== //
    // ========================== СТРОКА СТАТУСА =========================== //
    // ===================================================================== //

    @FXML
    public Label mainTradeStatusBar;

    private void showError(String errorMessage) {
        mainTradeStatusBar.setText(errorMessage);
        mainTradeStatusBar.getStyleClass().add("error-status");

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            mainTradeStatusBar.setText("");
            mainTradeStatusBar.getStyleClass().remove("error-status");
        });
        pause.play();
    }

    // ===================================================================== //
    // =============================== ОКНА ================================ //
    // ===================================================================== //

    /**
     * Открывает окно с детальной информацией об акции.
     */
    public void openWindowStockDetails() {
        FXMLLoader fxmlDocument = JavaFxUtils.openWindow("/stockdetails.fxml", "Информация об акции");
        StockDetailsController controller = fxmlDocument.getController();
        controller.initData(shareListManager.getSelectedItem());
    }

    /**
     * Открывает окно настроек со списком доступных для торговли акций.
     */
    public void openWindowAvailableStocks() {
        FXMLLoader fxmlDocument = JavaFxUtils.openWindow("/stocklist.fxml", "Выбор акций");
        StockListController controller = fxmlDocument.getController();
        controller.setCallback(this::addShare);
    }
}
