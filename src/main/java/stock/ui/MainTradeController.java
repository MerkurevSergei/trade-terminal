package stock.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.Share;
import stock.domain.balancer.Balancer;
import stock.shared.BeanRegister;
import stock.ui.trade.RevenueTableManager;
import stock.ui.trade.ShareListManager;
import stock.ui.trade.VolatilityTableManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Getter
public class MainTradeController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.shareListManager = new ShareListManager(fxmlListViewShare, BeanRegister.MAIN_SHARE_REPOSITORY);
        this.volatilityTableManager = new VolatilityTableManager(fxmlTableViewVolatility, BeanRegister.HISTORY_CLIENT);
        this.revenueTableManager = new RevenueTableManager(fxmlTableViewRevenue, new Balancer(BeanRegister.HISTORY_CLIENT));
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            Throwable rootCause = getRootCause(exception);
            Platform.runLater(() -> {
                showError(rootCause.getMessage());
            });
        });
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
    // ======================= ТАБЛИЦА ВОЛАТИЛЬНОСТИ ======================= //
    // ===================================================================== //

    @FXML
    private TableView<List<String>> fxmlTableViewVolatility;
    private VolatilityTableManager volatilityTableManager;

    public void calculateVolatility() {
        volatilityTableManager.calculateVolatility(shareListManager.getSelectedItem());
    }

    // ===================================================================== //
    // ======================= ТАБЛИЦА ДОХОДНОСТИ ======================= //
    // ===================================================================== //

    @FXML
    private TableView<Map.Entry<LocalDate, BigDecimal>> fxmlTableViewRevenue;
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
    // ================= НАСТРОЙКИ. СПИСОК ДОСТУПНЫХ АКЦИЙ ================= //
    // ===================================================================== //

    public void openWindowAvailableStocks() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/stocklist.fxml"));
        Parent stockListFxml = loader.load();
        StockListController controller = loader.getController();
        controller.setCallback(this::addShare);

        Stage childStage = new Stage();
        childStage.setTitle("Выбор акций");
        Scene scene = new Scene(stockListFxml);
        childStage.setScene(scene);
        childStage.initModality(Modality.APPLICATION_MODAL);
        childStage.show();
    }
}
