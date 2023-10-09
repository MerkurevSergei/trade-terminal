package stock.ui.trade;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;
import stock.client.HistoryClient;
import stock.domain.balancer.Balancer;
import stock.shared.Utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public record RevenueTableManager(TableView<Map.Entry<LocalDate, BigDecimal>> revenueTableView, Balancer balancer) {

    public RevenueTableManager {
        revenueTableView.getColumns().clear();
        TableColumn<Map.Entry<LocalDate, BigDecimal>, String> tableColumn = new TableColumn<>("Дата");
        TableColumn<Map.Entry<LocalDate, BigDecimal>, String> tableColumn2 = new TableColumn<>("Доход, %");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getKey().toString()));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
        revenueTableView.getColumns().add(tableColumn);
        revenueTableView.getColumns().add(tableColumn2);
    }

    public void calculateRevenue(Share selectedItem) {
        if (selectedItem == null) {
            throw new IllegalArgumentException("Не выбрана акция для расчета доходности");
        }
        LocalDate start = LocalDate.of(2023, Month.JUNE, 1);
        Map<LocalDate, BigDecimal> profit = balancer.getProfit(selectedItem.getFigi(), start, start.plusDays(120));
        revenueTableView.setItems(FXCollections.observableArrayList(profit.entrySet()));
    }
}
