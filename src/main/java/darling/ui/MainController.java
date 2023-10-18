package darling.ui;

import darling.context.MarketContext;
import darling.domain.Operation;
import darling.ui.view.PortfolioViewItem;
import darling.domain.order.Order;
import darling.robot.balancer.Balancer2;
import darling.shared.JavaFxUtils;
import darling.ui.main.ActiveOrderManager;
import darling.ui.main.MainShareManager;
import darling.ui.main.OperationsManager;
import darling.ui.main.PortfolioManager;
import darling.ui.main.RevenueTableManager;
import darling.ui.view.MainShareView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;
import lombok.Getter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static darling.shared.ApplicationProperties.SAND_MODE;
import static darling.shared.ApplicationProperties.TRADE_ON;

@Getter
public class MainController implements Initializable {

    @FXML
    public TableView<MainShareView> fxmlTableViewMainShares;
    private MainShareManager mainShareManager;

    @FXML
    public TableView<PortfolioViewItem> fxmlTableViewPortfolio;

    @FXML
    private TableView<Map.Entry<String, BigDecimal>> fxmlTableViewRevenue;
    private RevenueTableManager revenueTableManager;

    @FXML
    public TableView<Operation> fxmlTableViewOperations;

    @FXML
    private TableView<Order> fxmlTableViewActiveOrders;

    // ===================================================================== //
    // ========== БЛОК ИНИЦИАЛИЗАЦИИ И ПЕРЕКЛЮЧЕНИЯ РЕЖИМА РАБОТЫ ========== //
    // ===================================================================== //

    private MarketContext marketContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modeSwitcher.setOnAction(event -> initMarket(modeSwitcher.isSelected()));
        initMarket(SAND_MODE);
        this.revenueTableManager = new RevenueTableManager(fxmlTableViewRevenue, MarketContext.HISTORY_SERVICE);
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, exception) -> {
                    exception.printStackTrace();
                    Platform.runLater(() -> showError(getRootCause(exception).getMessage()));
                }
        );
    }

    /**
     * Настраивает и запускает контекст приложения, красит кнопочку.
     *
     * @param sandMode true - режим тестирования / false - режим торговли на бирже.
     */
    private void initMarket(boolean sandMode) {
        changeUiModeSwitcher(sandMode);
        if (marketContext != null) {
            marketContext.stop();
        }
        marketContext = new MarketContext(sandMode);
        OperationsManager operationsManager = new OperationsManager(fxmlTableViewOperations, marketContext);
        PortfolioManager portfolioManager = new PortfolioManager(fxmlTableViewPortfolio, marketContext);
        ActiveOrderManager activeOrderManager = new ActiveOrderManager(fxmlTableViewActiveOrders, marketContext);
        mainShareManager = new MainShareManager(fxmlTableViewMainShares, marketContext);
        marketContext.addListener(operationsManager);
        marketContext.addListener(portfolioManager);
        marketContext.addListener(mainShareManager);
        marketContext.addListener(activeOrderManager);
        if (TRADE_ON) {
            Balancer2 balancer2 = new Balancer2(marketContext);
            marketContext.addListener(balancer2);
        }
        marketContext.start();
    }

    // ===================================================================== //
    // ================= ДОСТУПНЫЕ ДЕЙСТВИЯ В ОСНОВНОМ ОКНЕ ================ //
    // ===================================================================== //

    public void deleteMainShare() {
        mainShareManager.deleteMainShare();
    }

    public void calculateRevenue() {
        //revenueTableManager.calculateRevenue(ShareMapper.INST.map(mainShareManager.getSelectedItem()));
    }


    // ===================================================================== //
    // =============================== ОКНА ================================ //
    // ===================================================================== //

    /**
     * Открывает окно с детальной информацией об акции.
     */
    public void openWindowStockDetails() {
        JavaFxUtils.openWindow("/stockdetails.fxml", "Информация об акции", marketContext);
    }

    /**
     * Открывает окно настроек со списком доступных для торговли акций.
     */
    public void openWindowAvailableStocks() {
        JavaFxUtils.openWindow("/stocklist.fxml", "Список доступных акций", marketContext);
    }

    // ===================================================================== //
    // ====================== СТРОКА СТАТУСА И ПРОЧЕЕ ====================== //
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

    @FXML
    public ToggleButton modeSwitcher;

    private void changeUiModeSwitcher(boolean sandMode) {
        modeSwitcher.setSelected(sandMode);
        if (sandMode) {
            modeSwitcher.setText("Песочница");
            modeSwitcher.setStyle("-fx-background-color:#36D100");
        } else {
            modeSwitcher.setText("Торговля");
            modeSwitcher.setStyle("-fx-background-color:red");
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause != null) ? getRootCause(cause) : throwable;
    }
}