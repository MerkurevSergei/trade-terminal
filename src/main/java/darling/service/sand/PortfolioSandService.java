package darling.service.sand;

import darling.domain.Deal;
import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.repository.DealRepository;
import darling.repository.OperationRepository;
import darling.service.PortfolioService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class PortfolioSandService implements PortfolioService {

    private final DealRepository dealRepository;

    private final OperationRepository operationRepository;

    /**
     * Возвращает признак наличия закрытых сделок на шаге.
     */
    @Override
    public boolean refreshPortfolio() {
        List<Deal> allDeals = dealRepository.findAllOpenDeals();
        Portfolio portfolio = new Portfolio(allDeals);
        List<Operation> allOperations = operationRepository.popFromQueue().stream()
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        allOperations.forEach(portfolio::refresh);
        List<Deal> deals = portfolio.getOpenDeals()
                .stream()
                .filter(deal -> deal.getQuantity() != 0)
                .toList();
        dealRepository.clearAndSaveOpenDeals(deals);
        dealRepository.saveClosedDeals(portfolio.getClosedDeals());
        return !portfolio.getClosedDeals().isEmpty();
    }

    @Override
    public void savePortfolio(Portfolio portfolio) {
        dealRepository.clearAndSaveOpenDeals(portfolio.getOpenDeals());
    }

    @Override
    public Portfolio getPortfolio() {
        return new Portfolio(dealRepository.findAllOpenDeals());
    }

    @Override
    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return dealRepository.getClosedDeals(start, end);
    }
}