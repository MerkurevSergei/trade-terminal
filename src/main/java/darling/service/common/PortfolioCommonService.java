package darling.service.common;

import darling.domain.OpenDeal;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.domain.PortfolioViewItem;
import darling.domain.Share;
import darling.repository.DealRepository;
import darling.service.InstrumentService;
import darling.service.OperationService;
import darling.service.PortfolioService;
import darling.shared.Utils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PortfolioCommonService implements PortfolioService {

    private final OperationService operationService;

    private final InstrumentService instrumentService;

    private final DealRepository dealRepository = new DealRepository();

    private Portfolio portfolio = new Portfolio(List.of());

    @Override
    public void refreshPortfolio() {
        List<OpenDeal> allOpenDeals = dealRepository.findAll();
        portfolio = new Portfolio(allOpenDeals);
        // TODO: заменить на получение необработанных операций
        List<Operation> allOperations = operationService.getAllOperations().stream()
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        allOperations.forEach(portfolio::refresh);
        // TODO: убрать заглушку и сохранять в БД
        dealRepository.saveAll(portfolio.getDeals());
    }

    @Override
    public List<PortfolioViewItem> getView() {
        Map<String, Share> sharesDict = instrumentService.getAvailableSharesDict();
        List<OpenDeal> deals = portfolio.getDeals();
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

    private PortfolioViewItem createPortfolioViewItem(OpenDeal deal, Map<String, Share> sharesDict) {
        Share share = sharesDict.get(deal.getInstrumentUid());
        return PortfolioViewItem.builder()
                .ticker(share.ticker())
                .date(deal.getDate().toString())
                .direction(Utils.accountName(deal.getAccountId()))
                .price(deal.getPrice().multiply(BigDecimal.valueOf(share.lot())).toString())
                .payment(deal.getPayment().toString())
                .quantity(deal.getQuantity() / share.lot())
                .build();
    }
}