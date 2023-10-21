package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.Share;
import darling.shared.CommonUtils;
import darling.shared.FinUtils;
import darling.ui.view.RevenueViewItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static darling.context.event.Event.CLOSED_DEALS_UPDATED;
import static darling.context.event.Event.CONTEXT_STARTED;

public record RevenueTableManager(TableView<RevenueViewItem> revenueTableView,
                                  MarketContext marketContext) implements EventListener {

    public RevenueTableManager {
        TableColumn<RevenueViewItem, String> tableColumnTicker = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(0);
        TableColumn<RevenueViewItem, String> tableColumnD = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(1);
        TableColumn<RevenueViewItem, String> tableColumnQuantity = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(2);
        TableColumn<RevenueViewItem, String> tableColumnProfitMoney = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(3);
        TableColumn<RevenueViewItem, String> tableColumnProfitCommission = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(4);
        TableColumn<RevenueViewItem, String> tableColumnProfitRevenue = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(5);
        tableColumnTicker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTicker()));
        tableColumnD.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDirection()));
        tableColumnQuantity.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getQuantity())));
        tableColumnProfitMoney.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getProfitMoney())));
        tableColumnProfitCommission.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getCommission())));
        tableColumnProfitRevenue.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getRevenue())));
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
        Map<String, Share> sharesDict = marketContext.getAvailableShares()
                .stream()
                .collect(Collectors.toMap(darling.domain.Share::uid, Function.identity()));

        // Исходные данные
        Map<String, List<RevenueViewItem>> viewItemByTicker = closedDeals
                .stream()
                .map(d -> createClosedViewItem(d, sharesDict))
                .collect(Collectors.groupingBy(RevenueViewItem::getTicker));

        // Итоги
        List<RevenueViewItem> view = new ArrayList<>();
        for (Map.Entry<String, List<RevenueViewItem>> groups : viewItemByTicker.entrySet()) {
            List<RevenueViewItem> group = groups.getValue();
            String profitMoneyTotal = group.stream()
                    .map(RevenueViewItem::getProfitMoney)
                    .reduce((s, s2) -> String.valueOf(new BigDecimal(s).add(new BigDecimal(s2))))
                    .orElse("-");
            String commission = group.stream()
                    .map(RevenueViewItem::getCommission)
                    .reduce((s, s2) -> String.valueOf(new BigDecimal(s).add(new BigDecimal(s2))))
                    .orElse("-");
            String revenue = group.stream()
                    .map(RevenueViewItem::getRevenue)
                    .reduce((s, s2) -> String.valueOf(new BigDecimal(s).add(new BigDecimal(s2))))
                    .orElse("-");
            RevenueViewItem head = RevenueViewItem.builder()
                    .ticker(groups.getKey())
                    .quantity("")
                    .profitMoney(profitMoneyTotal)
                    .commission(commission)
                    .revenue(revenue)
                    .build();
            group.forEach(portfolioViewItem -> portfolioViewItem.setTicker(""));
            view.add(head);
            view.addAll(group);
        }
        return view;
    }

    private RevenueViewItem createClosedViewItem(Deal d, Map<String, Share> sharesDict) {
        Share share = sharesDict.get(d.getInstrumentUid());
        long lotQuantity = d.getQuantity() / share.lot();

        BigDecimal profitMoney = FinUtils.getProfitMoney(d.getOpenPrice(), d.getClosePrice(), d.getType()).multiply(BigDecimal.valueOf(d.getQuantity()));

        BigDecimal commissionLotOpen = d.getOpenOperation().commission().divide(BigDecimal.valueOf(d.getOpenOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commissionLotClose = d.getCloseOperation().commission().divide(BigDecimal.valueOf(d.getCloseOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commission = commissionLotOpen.add(commissionLotClose).multiply(BigDecimal.valueOf(lotQuantity)).abs();
        return RevenueViewItem.builder()
                .ticker(share.ticker())
                .direction(CommonUtils.direction(d.getAccountId(), d.getType()))
                .quantity(String.valueOf(lotQuantity))
                .profitMoney(profitMoney.setScale(2, RoundingMode.HALF_UP).toString())
                .commission(commission.setScale(2, RoundingMode.HALF_UP).toString())
                .revenue(profitMoney.subtract(commission).setScale(2, RoundingMode.HALF_UP).toString())

                .build();
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
