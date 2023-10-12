package darling.ui.main;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.Share;
import darling.service.HistoryService;
import darling.domain.robot.balancer.Balancer;
import darling.domain.robot.balancer.StatRecord;
import darling.domain.HistoricPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RevenueTableManager(TableView<Map.Entry<String, BigDecimal>> revenueTableView,
                                  HistoryService historyService) {

    public RevenueTableManager {
        revenueTableView.getColumns().clear();
        TableColumn<Map.Entry<String, BigDecimal>, String> tableColumn = new TableColumn<>("Дата");
        TableColumn<Map.Entry<String, BigDecimal>, String> tableColumn2 = new TableColumn<>("Доход, %");
        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getKey()));
        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().setScale(2, RoundingMode.HALF_UP) + "%"));
        revenueTableView.getColumns().add(tableColumn);
        revenueTableView.getColumns().add(tableColumn2);
    }

    public void calculateRevenue(Share selectedItem) {
        if (selectedItem == null) {
            throw new IllegalArgumentException("Не выбрана акция для расчета доходности");
        }
        Map<String, BigDecimal> profitByDay = new LinkedHashMap<>();
        LocalDate start = LocalDate.of(2023, Month.SEPTEMBER, 1);
        Map<LocalDateTime, List<HistoricPoint>> pointsByDay= new LinkedHashMap<>();
        for (int i = 0; i < 30; i++) {
            LocalDateTime currentDay = start.plusDays(i).atStartOfDay();
            List<HistoricPoint> points = historyService.getMinutePointsByDay(selectedItem.getFigi(), currentDay, currentDay.plusDays(1));
            if (points.isEmpty() || currentDay.getDayOfWeek().equals(DayOfWeek.SUNDAY) || currentDay.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                continue;
            }
            pointsByDay.put(currentDay, points);

        }
        for (int j = 100; j < 1000; j=j+50) {
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<LocalDateTime, List<HistoricPoint>> pbd : pointsByDay.entrySet()) {
                List<HistoricPoint> points = pbd.getValue();
                BigDecimal profitDelta = getProfitDelta(points.get(0).price(), BigDecimal.valueOf(j/1000.00));
                BigDecimal levelGap = levelGap(profitDelta);
                List<StatRecord> statisticByDay = new Balancer(UUID.randomUUID().toString(), profitDelta, levelGap).getProfitSum(points);
                total = total.add(statisticByDay.stream().map(StatRecord::profitPercent).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));

            }
            profitByDay.put(String.valueOf(j), total);
        }
        //BigDecimal sum = profitByDay.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        //statisticByDay.sort(Comparator.comparing(o -> o.bet().getTime()));
        //Optional<BigDecimal> optSum = profitByDay.values().stream().reduce(BigDecimal::add);
        //profitByDay.put(LocalDate.MAX, optSum.orElse(BigDecimal.ZERO));
        revenueTableView.setItems(FXCollections.observableArrayList(profitByDay.entrySet()));
    }

    private BigDecimal getProfitDelta(BigDecimal price, BigDecimal percent) {
        int scale = price.scale();
        BigDecimal ratio = percent.divide(BigDecimal.valueOf(100), scale, RoundingMode.HALF_UP);
        return price.multiply(ratio);
    }

    private BigDecimal levelGap(BigDecimal profitDelta) {
        return profitDelta.divide(BigDecimal.valueOf(10), profitDelta.scale(), RoundingMode.HALF_UP);
    }
}
