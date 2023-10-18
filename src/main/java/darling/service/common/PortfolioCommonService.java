package darling.service.common;

import darling.domain.Deal;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.repository.DealRepository;
import darling.repository.OperationRepository;
import darling.service.InstrumentService;
import darling.service.PortfolioService;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

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
        List<Deal> deals = portfolio.getOpenDeals()
                .stream()
                .filter(deal -> deal.getQuantity() != 0)
                .toList();
        dealRepository.refreshOpenDeals(deals);
    }

    @Override
    public void savePortfolio(Portfolio portfolio) {
        dealRepository.refreshOpenDeals(portfolio.getOpenDeals());
    }

    @Override
    public Portfolio getPortfolio() {
        return new Portfolio(dealRepository.findAllOpenDeals());
    }
}