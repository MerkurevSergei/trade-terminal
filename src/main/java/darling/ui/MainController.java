package darling.ui;

import darling.context.MarketContext;
import darling.context.SandMarketContext;
import darling.context.TinkoffMarketContext;
import darling.domain.positions.model.Operation;
import darling.shared.JavaFxUtils;
import darling.ui.main.OperationsManager;
import darling.ui.main.RevenueTableManager;
import darling.ui.main.ShareListManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.Share;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static darling.shared.ApplicationProperties.SAND_MODE;

@Getter
public class MainController implements Initializable {

    @FXML
    public TableView<Operation> fxmlTableViewOperations;
    private OperationsManager operationsManager;

    // ===================================================================== //
    // ========== БЛОК ИНИЦИАЛИЗАЦИИ И ПЕРЕКЛЮЧЕНИЕ РЕЖИМА РАБОТЫ ========== //
    // ===================================================================== //

    private MarketContext marketContext;

    @FXML
    public ToggleButton modeSwitcher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modeSwitcher.setOnAction(event -> initMarket(modeSwitcher.isSelected()));
        initMarket(SAND_MODE);
        this.shareListManager = new ShareListManager(fxmlListViewShare, SandMarketContext.MAIN_SHARE_REPOSITORY);
        this.revenueTableManager = new RevenueTableManager(fxmlTableViewRevenue, SandMarketContext.HISTORY_CLIENT);
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

    /**
     * Настраивает и запускает контекст приложения, красит кнопочку.
     *
     * @param sandMode true - режим тестирования / false - режим торговли на бирже.
     */
    private void initMarket(boolean sandMode) {
        modeSwitcher.setSelected(sandMode);
        if (sandMode) {
            modeSwitcher.setText("Песочница");
            modeSwitcher.setStyle("-fx-background-color:#36D100");
        } else {
            modeSwitcher.setText("Торговля");
            modeSwitcher.setStyle("-fx-background-color:red");
        }

        if (marketContext != null) {
            marketContext.stop();
        }
        marketContext = sandMode ? new SandMarketContext() : new TinkoffMarketContext();
        this.operationsManager = new OperationsManager(fxmlTableViewOperations, marketContext);
        marketContext.start();
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