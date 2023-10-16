package darling.service;

import darling.domain.Portfolio;

public interface PortfolioService {

    Portfolio getView();

    void refreshPortfolio();
}
