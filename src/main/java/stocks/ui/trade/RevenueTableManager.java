package stocks.ui.trade;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.Share;
import stocks.client.HistoryClient;
import stocks.domain.balancer.Balancer;
import stocks.domain.balancer.StatRecord;
import stocks.domain.model.HistoricPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record RevenueTableManager(TableView<Map.Entry<LocalDate, BigDecimal>> revenueTableView, HistoryClient historyClient) {

    public RevenueTableManager {
        revenueTableView.getColumns().clear();
        TableColumn<Map.Entry<LocalDate, BigDecimal>, String> tableColumn = new TableColumn<>("Дата");
        TableColumn<Map.Entry<LocalDate, BigDecimal>, String> tableColumn2 = new TableColumn<>("Доход, %");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getKey().toString()));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().setScale(2, RoundingMode.HALF_UP) + "%"));
        revenueTableView.getColumns().add(tableColumn);
        revenueTableView.getColumns().add(tableColumn2);
    }

    public void calculateRevenue(Share selectedItem) {
        if (selectedItem == null) {
            throw new IllegalArgumentException("Не выбрана акция для расчета доходности");
        }
        Map<LocalDate, BigDecimal> profitByDay = new LinkedHashMap<>();
        LocalDate start = LocalDate.of(2023, Month.SEPTEMBER, 1);
        for (int i = 0; i < 30; i++) {
            LocalDateTime currentDay = start.plusDays(i).atStartOfDay();
            if (currentDay.getDayOfWeek().equals(DayOfWeek.SUNDAY) || currentDay.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                continue;
            }
            List<HistoricPoint> points = historyClient.getMinutePointsByDay(selectedItem.getFigi(), currentDay, currentDay.plusDays(1));
            List<StatRecord> statisticByDay = new Balancer().getProfitSum(points);
            statisticByDay.sort(Comparator.comparing(o -> o.bet().getTime()));
            profitByDay.put(currentDay.toLocalDate(), statisticByDay.stream().map(StatRecord::profitPercent).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        }
        Optional<BigDecimal> optSum = profitByDay.values().stream().reduce(BigDecimal::add);
        profitByDay.put(LocalDate.MAX, optSum.orElse(BigDecimal.ZERO));
        revenueTableView.setItems(FXCollections.observableArrayList(profitByDay.entrySet()));
    }
}
