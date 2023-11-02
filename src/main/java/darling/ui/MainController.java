package darling.ui;

import darling.context.MarketContext;
import darling.domain.MainShare;
import darling.domain.Operation;
import darling.domain.order.Order;
import darling.robot.Balancer2;
import darling.shared.JavaFxUtils;
import darling.ui.main.ActiveOrderManager;
import darling.ui.main.MainShareManager;
import darling.ui.main.OperationsManager;
import darling.ui.main.PortfolioManager;
import darling.ui.main.RevenueTableManager;
import darling.ui.view.MainShareView;
import darling.ui.view.PortfolioViewItem;
import darling.ui.view.RevenueViewItem;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.time.LocalTime.MAX;

@Getter
public class MainController implements Initializable {

    @FXML
    public TableView<MainShareView> fxmlTableViewMainShares;
    private MainShareManager mainShareManager;

    @FXML
    public TabPane fxmlTabPanePortfolioAndCharts;

    @FXML
    public TableView<PortfolioViewItem> fxmlTableViewPortfolio;

    @FXML
    private TableView<RevenueViewItem> fxmlTableViewRevenue;
    @FXML
    private DatePicker fxmlDatePickerOnDay;
    @FXML
    private Button fxmlButtonOnPeriod;
    private RevenueTableManager revenueTableManager;

    @FXML
    public TableView<Operation> fxmlTableViewOperations;

    @FXML
    private TableView<Order> fxmlTableViewActiveOrders;

    // ===================================================================== //
    // ======================== БЛОК ИНИЦИАЛИЗАЦИИ ========================= //
    // ===================================================================== //

    private MarketContext marketContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxmlDatePickerEmulateStart.setValue(LocalDate.now(ZoneOffset.UTC).minusDays(18));
        fxmlDatePickerEmulateEnd.setValue(LocalDate.now(ZoneOffset.UTC).minusDays(18));
        fxmlTabPanePortfolioAndCharts.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        initMarket(true, false);
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
     * @param robotOn  true - робот может торговать, false - робот не может торговать.
     */
    private void initMarket(boolean sandMode, boolean robotOn) {
        if (marketContext != null) {
            marketContext.stop();
        }
        marketContext = new MarketContext(sandMode);
        ActiveOrderManager activeOrderManager = new ActiveOrderManager(fxmlTableViewActiveOrders, marketContext);
        OperationsManager operationsManager = new OperationsManager(fxmlTableViewOperations, marketContext);
        PortfolioManager portfolioManager = new PortfolioManager(fxmlTableViewPortfolio, marketContext);
        revenueTableManager = new RevenueTableManager(fxmlTableViewRevenue, fxmlDatePickerOnDay, fxmlButtonOnPeriod, marketContext);
        mainShareManager = new MainShareManager(fxmlTableViewMainShares, marketContext);
        marketContext.addListener(activeOrderManager);
        marketContext.addListener(operationsManager);
        marketContext.addListener(portfolioManager);
        marketContext.addListener(mainShareManager);
        marketContext.addListener(revenueTableManager);
        addRobotToMarketContext(sandMode, robotOn);
        marketContext.start();
        if (sandMode && robotOn) {
            marketContext.rewindHistory(fxmlChoiceTestShare.getValue().uid(),
                                        fxmlDatePickerEmulateStart.getValue().atStartOfDay(),
                                        fxmlDatePickerEmulateEnd.getValue().atTime(MAX));
        }
        fxmlChoiceTestShare.getItems().addAll(marketContext.getMainShares().stream().toList());
    }

    private void addRobotToMarketContext(boolean sandMode, boolean robotOn) {
        if (!robotOn) {
            robots.forEach(marketContext::removeListener);
            robots.clear();
            return;
        }
        List<MainShare> tradingShares = new ArrayList<>();
        if (sandMode) {
            if (fxmlChoiceTestShare.getValue() == null)
                throw new IllegalStateException("Не выбрана акция для загрузки истории");
            tradingShares.add(fxmlChoiceTestShare.getValue());
        } else {
            tradingShares = marketContext.getMainShares().stream().filter(MainShare::isTrade).toList();
        }
        for (MainShare tradingShare : tradingShares) {
            Balancer2 balancer2 = new Balancer2(marketContext, tradingShare, !sandMode);
            robots.add(balancer2);
            marketContext.addListener(balancer2);
        }
    }

    // ===================================================================== //
    // ==================== ПЕРЕКЛЮЧЕНИЕ РЕЖИМОВ РАБОТЫ ==================== //
    // ===================================================================== //

    @FXML
    public ToggleButton fxmlModeSwitcher;
    @FXML
    public ToggleButton fxmlToggleOnOffRobot;
    @FXML
    public DatePicker fxmlDatePickerEmulateStart;
    @FXML
    public DatePicker fxmlDatePickerEmulateEnd;
    @FXML
    public ChoiceBox<MainShare> fxmlChoiceTestShare;

    private final List<Balancer2> robots = new ArrayList<>();

    /**
     * Переключение режима песочница / торговля.
     */
    public void fxmlSwitchSandLive() {
        boolean isSandMode = !fxmlModeSwitcher.isSelected();
        initMarket(isSandMode, false);
        if (isSandMode) {
            fxmlModeSwitcher.setText("Песочница");
            fxmlModeSwitcher.setStyle("-fx-background-color:#36D100");

            fxmlToggleOnOffRobot.setSelected(false);
            fxmlToggleOnOffRobot.setText("Запустить робота");

            fxmlDatePickerEmulateStart.setDisable(false);
            fxmlDatePickerEmulateEnd.setDisable(false);
        } else {
            fxmlModeSwitcher.setText("Торговля");
            fxmlModeSwitcher.setStyle("-fx-background-color:red");

            fxmlDatePickerEmulateStart.setDisable(true);
            fxmlDatePickerEmulateEnd.setDisable(true);
        }
    }

    /**
     * Запустить робота.
     */
    public void fxmlRobotOnOff() {
        boolean isSandMode = !fxmlModeSwitcher.isSelected();
        boolean robotTradeOn = fxmlToggleOnOffRobot.isSelected();
//        if (!robotTradeOn) return;
//        BigDecimal I2 = PERCENT_DELTA_PROFIT;
//        BigDecimal I3 = PERCENT_PROFIT_CLEAR_LAG;
//        for (int i = 0; i < 3; i++) {
//            PERCENT_DELTA_PROFIT_TRIGGER = PERCENT_DELTA_PROFIT_TRIGGER.add(new BigDecimal("0.01"));
//            for (int j = 0; j < 3; j++) {
//                PERCENT_DELTA_PROFIT = PERCENT_DELTA_PROFIT.add(new BigDecimal("0.02"));
//                for (int k = 0; k < 3; k++) {
//                    PERCENT_PROFIT_CLEAR_LAG = PERCENT_PROFIT_CLEAR_LAG.add(new BigDecimal("2"));
//                    initMarket(isSandMode, robotTradeOn);
//                }
//                PERCENT_PROFIT_CLEAR_LAG = I3;
//            }
//            PERCENT_DELTA_PROFIT = I2;
//        }
        initMarket(isSandMode, robotTradeOn);
        if (robotTradeOn) {
            fxmlToggleOnOffRobot.setText("Остановить робота");
        } else {
            fxmlToggleOnOffRobot.setText("Запустить робота");
        }
    }

    // ===================================================================== //
    // ================= ДОСТУПНЫЕ ДЕЙСТВИЯ В ОСНОВНОМ ОКНЕ ================ //
    // ===================================================================== //

    /**
     * Удалить акцию из списка.
     */
    public void fxmlDeleteMainShare() {
        mainShareManager.deleteMainShare();
    }

    /**
     * Показать список закрытых сделок за выбранный день.
     */
    public void fxmlShowClosedDealsOnDay() {
        revenueTableManager.calculateRevenue(true);
    }

    /**
     * Показать прибыль по дням.
     */
    public void fxmlShowClosedDealsTotalByDays() {
        revenueTableManager.calculateRevenue(false);
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

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause != null) ? getRootCause(cause) : throwable;
    }

    public void onMouseClickedAddNewChart(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)) {
            fxmlTabPanePortfolioAndCharts.getTabs().add(new Tab(fxmlTableViewMainShares.getSelectionModel().getSelectedItem().share().ticker()));
        }
    }
}