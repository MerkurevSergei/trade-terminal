package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.ui.view.RevenueViewItem;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import ru.tinkoff.piapi.contract.v1.Share;

import java.util.List;
import java.util.Objects;

import static darling.context.event.Event.CLOSED_DEALS_UPDATED;
import static darling.context.event.Event.CONTEXT_STARTED;

public record RevenueTableManager(TableView<RevenueViewItem> revenueTableView,
                                  MarketContext marketContext) implements EventListener {

    public RevenueTableManager {
//        revenueTableView.getColumns().clear();
//        TableColumn<Map.Entry<String, BigDecimal>, String> tableColumn = new TableColumn<>("Дата");
//        TableColumn<Map.Entry<String, BigDecimal>, String> tableColumn2 = new TableColumn<>("Доход, %");
//        tableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getKey()));
//        tableColumn2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().setScale(2, RoundingMode.HALF_UP) + "%"));
//        revenueTableView.getColumns().add(tableColumn);
//        revenueTableView.getColumns().add(tableColumn2);
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, CLOSED_DEALS_UPDATED) && !Objects.equals(event, CONTEXT_STARTED)) {
            return;
        }
        List<Deal> closedDeals = marketContext.getClosedDeals();
        revenueTableView.setItems(FXCollections.observableArrayList(getView(closedDeals)));
    }

    private List<RevenueViewItem> getView(List<Deal> closedDeals) {
        return List.of();
    }

    public void calculateRevenue(Share selectedItem) {
//        if (selectedItem == null) {
//            throw new IllegalArgumentException("Не выбрана акция для расчета доходности");
//        }
//        Map<String, BigDecimal> profitByDay = new LinkedHashMap<>();
//        LocalDate start = LocalDate.of(2023, Month.SEPTEMBER, 1);
//        Map<LocalDateTime, List<HistoricPoint>> pointsByDay= new LinkedHashMap<>();
//        for (int i = 0; i < 30; i++) {
//            LocalDateTime currentDay = start.plusDays(i).atStartOfDay();
//            List<HistoricPoint> points = historyService.getMinutePointsByDay(selectedItem.getFigi(), currentDay, currentDay.plusDays(1));
//            if (points.isEmpty() || currentDay.getDayOfWeek().equals(DayOfWeek.SUNDAY) || currentDay.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
//                continue;
//            }
//            pointsByDay.put(currentDay, points);
//
//        }
//        for (int j = 100; j < 1000; j=j+50) {
//            BigDecimal total = BigDecimal.ZERO;
//            for (Map.Entry<LocalDateTime, List<HistoricPoint>> pbd : pointsByDay.entrySet()) {
//                List<HistoricPoint> points = pbd.getValue();
//                BigDecimal profitDelta = getProfitDelta(points.get(0).price(), BigDecimal.valueOf(j/1000.00));
//                BigDecimal levelGap = levelGap(profitDelta);
//                List<StatRecord> statisticByDay = new Balancer(UUID.randomUUID().toString(), profitDelta, levelGap).getProfitSum(points);
//                total = total.add(statisticByDay.stream().map(StatRecord::profitPercent).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
//
//            }
//            profitByDay.put(String.valueOf(j), total);
//        }
//        //BigDecimal sum = profitByDay.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
//        //statisticByDay.sort(Comparator.comparing(o -> o.bet().getTime()));
//        //Optional<BigDecimal> optSum = profitByDay.values().stream().reduce(BigDecimal::add);
//        //profitByDay.put(LocalDate.MAX, optSum.orElse(BigDecimal.ZERO));
//        revenueTableView.setItems(FXCollections.observableArrayList(profitByDay.entrySet()));
    }
}
