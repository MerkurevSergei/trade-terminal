package darling.service;

import darling.domain.Deal;
import darling.domain.Portfolio;

import java.util.List;

public interface PortfolioService {

    boolean refreshPortfolio();

    void savePortfolio(Portfolio portfolio);

    Portfolio getPortfolio();

    List<Deal> getClosedDeals();
}
