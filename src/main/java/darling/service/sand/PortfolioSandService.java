package darling.service.sand;

import darling.domain.Deal;
import darling.domain.Portfolio;
import darling.repository.DealRepository;
import darling.service.PortfolioService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PortfolioSandService implements PortfolioService {

    private final DealRepository dealDbRepository;

    /**
     * Возвращает признак наличия закрытых сделок на шаге.
     */
    @Override
    public boolean refreshPortfolio() {
        List<Deal> allDeals = dealDbRepository.findAllOpenDeals();
        Portfolio portfolio = new Portfolio(allDeals);
//        List<Operation> allOperations = operationRepository.popFromQueue().stream()
//                .sorted(Comparator.comparing(Operation::date))
//                .toList();
//        allOperations.forEach(portfolio::refresh);
//        List<Deal> deals = portfolio.getOpenDeals()
//                .stream()
//                .filter(deal -> deal.getQuantity() != 0)
//                .toList();
//        dealRepository.refreshOpenDeals(deals);
//        dealRepository.saveClosedDeals(portfolio.getClosedDeals());
        return !portfolio.getClosedDeals().isEmpty();
    }

    @Override
    public void savePortfolio(Portfolio portfolio) {
        dealDbRepository.refreshOpenDeals(portfolio.getOpenDeals());
    }

    @Override
    public Portfolio getPortfolio() {
        return new Portfolio(dealDbRepository.findAllOpenDeals());
    }

    @Override
    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        return dealDbRepository.getClosedDeals(start, end);
    }
}