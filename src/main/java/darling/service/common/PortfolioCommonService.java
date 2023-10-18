package darling.service.common;

import darling.domain.Deal;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.domain.PortfolioViewItem;
import darling.domain.Share;
import darling.repository.DealRepository;
import darling.repository.OperationRepository;
import darling.service.InstrumentService;
import darling.service.PortfolioService;
import darling.shared.Utils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PortfolioCommonService implements PortfolioService {

    private final InstrumentService instrumentService;

    private final DealRepository dealRepository = new DealRepository();

    private final OperationRepository operationRepository = new OperationRepository();

    private Portfolio portfolio = new Portfolio(List.of());

    @Override
    public void refreshPortfolio() {
        List<Deal> allDeals = dealRepository.findAllOpenDeals();
        portfolio = new Portfolio(allDeals);
        List<Operation> allOperations = operationRepository.popFromQueue().stream()
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        allOperations.forEach(portfolio::refresh);
        dealRepository.refreshOpenDeals(portfolio.getOpenDeals());
    }

    @Override
    public void savePortfolio(Portfolio portfolio) {
        dealRepository.refreshOpenDeals(portfolio.getOpenDeals());
    }

    @Override
    public Portfolio getPortfolio() {
        return new Portfolio(dealRepository.findAllOpenDeals());
    }

    @Override
    public List<PortfolioViewItem> getView() {
        Map<String, Share> sharesDict = instrumentService.getAvailableSharesDict();
        List<Deal> deals = portfolio.getOpenDeals();
        Map<String, List<PortfolioViewItem>> viewItemByTicker = deals
                .stream()
                .map(d -> createPortfolioViewItem(d, sharesDict))
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

    private PortfolioViewItem createPortfolioViewItem(Deal deal, Map<String, Share> sharesDict) {
        Share share = sharesDict.get(deal.getInstrumentUid());
        BigDecimal lotPrice = deal.getPrice().multiply(BigDecimal.valueOf(share.lot()));
        BigDecimal takeProfitPrice = deal.getTakeProfitPrice().multiply(BigDecimal.valueOf(share.lot()));
        long lotQuantity = deal.getQuantity() / share.lot();
        return PortfolioViewItem.builder()
                .ticker(share.ticker())
                .date(deal.getDate())
                .direction(Utils.accountName(deal.getAccountId()))
                .price(lotPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .takeProfitPrice(takeProfitPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .payment(lotPrice.multiply(BigDecimal.valueOf(lotQuantity)).setScale(2, RoundingMode.HALF_UP).toString())
                .quantity(lotQuantity)
                .build();
    }
}