package darling.service;

import darling.domain.Portfolio;

public interface PortfolioService {

    void refreshPortfolio();

    void savePortfolio(Portfolio portfolio);

    Portfolio getPortfolio();
}
