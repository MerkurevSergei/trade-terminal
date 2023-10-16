package darling.service;

import darling.domain.PortfolioViewItem;

import java.util.List;

public interface PortfolioService {

    List<PortfolioViewItem> getView();

    void refreshPortfolio();
}
