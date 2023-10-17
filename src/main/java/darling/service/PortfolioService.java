package darling.service;

import darling.domain.Portfolio;
import darling.domain.PortfolioViewItem;

import java.util.List;

public interface PortfolioService {

    List<PortfolioViewItem> getView();

    void refreshPortfolio();

    Portfolio getPortfolio();
}
