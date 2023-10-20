package darling.ui.main;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Deal;
import darling.domain.LastPrice;
import darling.domain.Share;
import darling.shared.CommonUtils;
import darling.shared.FinUtils;
import darling.ui.view.PortfolioViewItem;
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

import static java.math.BigDecimal.ZERO;

public record PortfolioManager(TableView<PortfolioViewItem> portfolioTableView,
                               MarketContext marketContext) implements EventListener {

    public PortfolioManager {
        TableColumn<PortfolioViewItem, String> tableColumnTicker = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(0);
        TableColumn<PortfolioViewItem, String> tableColumnD = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(1);
        TableColumn<PortfolioViewItem, String> tableColumnQuantity = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(2);
        TableColumn<PortfolioViewItem, String> tableColumnPrice = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(3);
        TableColumn<PortfolioViewItem, String> tableColumnProfitPrice = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(4);
        TableColumn<PortfolioViewItem, String> tableColumnPayment = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(5);
        TableColumn<PortfolioViewItem, String> tableColumnProfitPercent = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(6);
        TableColumn<PortfolioViewItem, String> tableColumnProfitMoney = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(7);
        TableColumn<PortfolioViewItem, String> tableColumnDate = (TableColumn<PortfolioViewItem, String>) portfolioTableView.getColumns().get(8);
        tableColumnTicker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTicker()));
        tableColumnD.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDirection()));
        tableColumnQuantity.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().getQuantity())));
        tableColumnPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getPrice()));
        tableColumnProfitPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTakeProfitPrice()));
        tableColumnPayment.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getPayment()));
        tableColumnProfitPercent.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getProfitPercent()));
        tableColumnProfitMoney.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getProfitMoney()));
        tableColumnDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(CommonUtils.formatLDT(p.getValue().getDate())));
    }

    @Override
    public void handle(Event event) {
        if (!Objects.equals(event, Event.PORTFOLIO_REFRESHED)) {
            return;
        }
        portfolioTableView.setItems(FXCollections.observableArrayList(getView()));
    }

    private List<PortfolioViewItem> getView() {
        Map<String, Share> sharesDict = marketContext.getAvailableShares().stream().collect(Collectors.toMap(Share::uid, Function.identity()));
        Map<String, BigDecimal> lastPrices = marketContext.getLastPrices().stream().collect(Collectors.toMap(LastPrice::instrumentUid, LastPrice::price));
        List<Deal> deals = marketContext.getPortfolio().getOpenDeals();
        Map<String, List<PortfolioViewItem>> viewItemByTicker = deals
                .stream()
                .map(d -> createPortfolioViewItem(d, sharesDict, lastPrices))
                .collect(Collectors.groupingBy(PortfolioViewItem::getTicker));

        List<PortfolioViewItem> view = new ArrayList<>();
        for (Map.Entry<String, List<PortfolioViewItem>> groups : viewItemByTicker.entrySet()) {
            List<PortfolioViewItem> group = groups.getValue();
            long sum = group.stream().mapToLong(PortfolioViewItem::getQuantity).sum();
            PortfolioViewItem head = PortfolioViewItem.builder()
                    .ticker(groups.getKey())
                    .quantity(sum)
                    .build();
            group.forEach(portfolioViewItem -> portfolioViewItem.setTicker(""));
            view.add(head);
            view.addAll(group);
        }
        return view;

    }

    private PortfolioViewItem createPortfolioViewItem(Deal deal, Map<String, Share> sharesDict, Map<String, BigDecimal> lastPrices) {
        Share share = sharesDict.get(deal.getInstrumentUid());
        BigDecimal dealLotPrice = deal.getPrice().multiply(BigDecimal.valueOf(share.lot()));
        BigDecimal takeProfitPrice = deal.getTakeProfitPrice().multiply(BigDecimal.valueOf(share.lot()));
        long lotQuantity = deal.getQuantity() / share.lot();
        BigDecimal lastPrice = lastPrices.getOrDefault(deal.getInstrumentUid(), ZERO);
        PortfolioViewItem.PortfolioViewItemBuilder viewItemBuilder = PortfolioViewItem.builder()
                .ticker(share.ticker())
                .date(deal.getOpenDate())
                .direction(CommonUtils.direction(deal.getAccountId(), deal.getType()))
                .price(dealLotPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .takeProfitPrice(takeProfitPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .quantity(lotQuantity);

        if (!lastPrice.equals(ZERO)) {
            BigDecimal currentPayment = lastPrice.multiply(BigDecimal.valueOf(share.lot())).multiply(BigDecimal.valueOf(lotQuantity));
            BigDecimal dealPayment = dealLotPrice.multiply(BigDecimal.valueOf(lotQuantity));
            viewItemBuilder.payment(currentPayment.setScale(2, RoundingMode.HALF_UP).toString())
                    .profitPercent(FinUtils.getProfitPercentFormat(dealPayment, currentPayment, deal.getType()))
                    .profitMoney(FinUtils.getProfitMoneyFormat(dealPayment, currentPayment, deal.getType()));
        }
        return viewItemBuilder.build();
    }
}