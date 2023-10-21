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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static darling.context.event.Event.CLOSED_DEALS_UPDATED;
import static darling.context.event.Event.CONTEXT_STARTED;
import static java.time.LocalTime.MAX;
import static java.time.ZoneOffset.UTC;

public record RevenueTableManager(TableView<RevenueViewItem> revenueTableView,
                                  javafx.scene.control.DatePicker onDateDatePicker,
                                  javafx.scene.control.Button totalByPeriodButton,
                                  MarketContext marketContext) implements EventListener {

    public RevenueTableManager {
        TableColumn<RevenueViewItem, String> tableColumnTicker = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(0);
        TableColumn<RevenueViewItem, String> tableColumnD = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(1);
        TableColumn<RevenueViewItem, String> tableColumnQuantity = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(2);
        TableColumn<RevenueViewItem, String> tableColumnProfitRevenue = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(3);
        TableColumn<RevenueViewItem, String> tableColumnProfitCommission = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(4);
        TableColumn<RevenueViewItem, String> tableColumnProfitMoney = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(5);
        TableColumn<RevenueViewItem, String> tableColumnOpenPrice = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(6);
        TableColumn<RevenueViewItem, String> tableColumnClosePrice = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(7);
        TableColumn<RevenueViewItem, String> tableColumnOpenDate = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(8);
        TableColumn<RevenueViewItem, String> tableColumnCloseDate = (TableColumn<RevenueViewItem, String>) revenueTableView.getColumns().get(9);
        tableColumnTicker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTickerView()));
        tableColumnD.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDirectionView()));
        tableColumnQuantity.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getQuantityView()));
        tableColumnProfitRevenue.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getRevenueView()));
        tableColumnProfitCommission.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getCommissionView()));
        tableColumnProfitMoney.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getProfitMoneyView()));
        tableColumnOpenPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getOpenPriceView()));
        tableColumnClosePrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getClosePriceView())));
        tableColumnOpenDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getOpenDateView()));
        tableColumnCloseDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getCloseDateView()));
        onDateDatePicker.setValue(LocalDate.now(UTC));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, CLOSED_DEALS_UPDATED) && !Objects.equals(event, CONTEXT_STARTED)) {
            return;
        }
        calculateRevenue(true);
    }

    public void calculateRevenue(boolean onDay) {
        LocalDate reportDate = onDateDatePicker.getValue() == null ? LocalDate.now(UTC) : onDateDatePicker.getValue();
        if (onDay) {
            List<Deal> closedDeals = marketContext.getClosedDeals(reportDate.atStartOfDay(), reportDate.atTime(MAX));
            revenueTableView.setItems(FXCollections.observableArrayList(getView(closedDeals)));
        } else {
            List<Deal> closedDeals = marketContext.getClosedDeals(reportDate.atStartOfDay().minusDays(30), reportDate.atTime(MAX));
            revenueTableView.setItems(FXCollections.observableArrayList(getViewTotalByDays(closedDeals)));
        }
    }


    // ============================= ВНУТРИ ДНЯ ============================ //

    private List<RevenueViewItem> getView(List<Deal> closedDeals) {
        Map<String, Share> sharesDict = marketContext.getAvailableShares()
                .stream()
                .collect(Collectors.toMap(darling.domain.Share::uid, Function.identity()));

        // Исходные данные
        Map<String, List<RevenueViewItem>> viewItemByTicker = closedDeals
                .stream()
                .sorted(Comparator.comparing(Deal::getOpenDate))
                .map(d -> createClosedViewItem(d, sharesDict))
                .collect(Collectors.groupingBy(RevenueViewItem::getTicker, () -> new TreeMap<>(String::compareTo), Collectors.toList()));

        // Итоги
        List<RevenueViewItem> view = new ArrayList<>();
        for (Map.Entry<String, List<RevenueViewItem>> groups : viewItemByTicker.entrySet()) {
            List<RevenueViewItem> group = groups.getValue();
            BigDecimal profitMoneyTotal = group.stream().map(RevenueViewItem::getProfitMoney).reduce(BigDecimal::add).orElse(null);
            BigDecimal commission = group.stream().map(RevenueViewItem::getCommission).reduce(BigDecimal::add).orElse(null);
            BigDecimal revenue = group.stream().map(RevenueViewItem::getRevenue).reduce(BigDecimal::add).orElse(null);
            RevenueViewItem head = RevenueViewItem.builder()
                    .ticker(groups.getKey())
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
        BigDecimal profitMoney = FinUtils.getProfitMoney(d.getOpenPrice(), d.getClosePrice(), d.getType())
                .multiply(BigDecimal.valueOf(d.getQuantity()));

        BigDecimal commissionLotOpen = d.getOpenOperation().commission().divide(BigDecimal.valueOf(d.getOpenOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commissionLotClose = d.getCloseOperation().commission().divide(BigDecimal.valueOf(d.getCloseOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commission = commissionLotOpen.add(commissionLotClose).multiply(BigDecimal.valueOf(lotQuantity));
        return RevenueViewItem.builder()
                .ticker(share.ticker())
                .direction(CommonUtils.direction(d.getAccountId(), d.getType()))
                .quantity(lotQuantity)
                .profitMoney(profitMoney)
                .commission(commission)
                .revenue(profitMoney.add(commission))
                .openPrice(d.getOpenPrice().multiply(BigDecimal.valueOf(share.lot())))
                .closePrice(d.getClosePrice().multiply(BigDecimal.valueOf(share.lot())))
                .openDate(d.getOpenDate())
                .closeDate(d.getCloseDate())
                .dateTimeFormatter(DateTimeFormatter.ofPattern("HH:mm:ss (dd)"))
                .build();
    }

    // ============================== ПО ДНЯМ ============================== //

    private List<RevenueViewItem> getViewTotalByDays(List<Deal> closedDeals) {
        Map<String, Share> sharesDict = marketContext.getAvailableShares()
                .stream()
                .collect(Collectors.toMap(darling.domain.Share::uid, Function.identity()));

        // Исходные данные
        Map<String, List<Deal>> groupByTickerAndDate = closedDeals.stream()
                .collect(Collectors.groupingBy(Deal::getInstrumentUid));
        List<RevenueViewItem> view = new ArrayList<>();
        for (Map.Entry<String, List<Deal>> entry : groupByTickerAndDate.entrySet()) {
            Map<LocalDate, List<Deal>> groupByDate = entry.getValue().stream()
                    .collect(Collectors.groupingBy(it -> it.getCloseDate().toLocalDate()));
            for (Map.Entry<LocalDate, List<Deal>> entry2 : groupByDate.entrySet()) {
                Optional<RevenueViewItem> reduce = entry2.getValue()
                        .stream()
                        .map(it -> createClosedViewItemTotalByDays(it, sharesDict))
                        .reduce((r, r2) -> RevenueViewItem.builder()
                                .ticker(r.getTicker())
                                .closeDate(r.getCloseDate())
                                .profitMoney(r.getProfitMoney().add(r2.getProfitMoney()))
                                .commission(r.getCommission().add(r2.getCommission()))
                                .revenue(r.getRevenue().add(r2.getRevenue()))
                                .dateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yy"))
                                .build()
                        );
                reduce.ifPresent(view::add);
            }
        }

        // почистим
        view.sort(Comparator.comparing(RevenueViewItem::getTicker).thenComparing(RevenueViewItem::getCloseDateView));
        view.forEach(item -> {
                         item.setOpenDate(null);
                         item.setDirection("");
                         item.setQuantity(null);
                         item.setOpenPrice(null);
                         item.setClosePrice(null);
                     }
        );
        return view;
    }

    private RevenueViewItem createClosedViewItemTotalByDays(Deal d, Map<String, Share> sharesDict) {
        Share share = sharesDict.get(d.getInstrumentUid());
        long lotQuantity = d.getQuantity() / share.lot();
        BigDecimal profitMoney = FinUtils.getProfitMoney(d.getOpenPrice(), d.getClosePrice(), d.getType())
                .multiply(BigDecimal.valueOf(d.getQuantity()));

        BigDecimal commissionLotOpen = d.getOpenOperation().commission().divide(BigDecimal.valueOf(d.getOpenOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commissionLotClose = d.getCloseOperation().commission().divide(BigDecimal.valueOf(d.getCloseOperation().quantity() / share.lot()), 9, RoundingMode.HALF_UP);
        BigDecimal commission = commissionLotOpen.add(commissionLotClose).multiply(BigDecimal.valueOf(lotQuantity));
        return RevenueViewItem.builder()
                .ticker(share.ticker())
                .closeDate(d.getCloseDate())
                .profitMoney(profitMoney)
                .commission(commission)
                .revenue(profitMoney.add(commission))
                .dateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yy"))
                .build();
    }
}