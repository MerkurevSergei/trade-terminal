package darling.service.common;

import darling.domain.Operation;
import darling.domain.Portfolio;
import darling.repository.ContractRepository;
import darling.service.OperationService;
import darling.service.PortfolioService;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class PortfolioCommonService implements PortfolioService {

    private final OperationService operationService;

    private final ContractRepository contractRepository = new ContractRepository();

    @Override
    public Portfolio getView() {
        return new Portfolio();

    }

    @Override
    public void refreshPortfolio() {
        Portfolio portfolio = new Portfolio();
        List<Operation> allOperations = operationService.getAllOperations().stream()
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        for (Operation o : allOperations) {
            portfolio.refresh(o);
        }
    }
}