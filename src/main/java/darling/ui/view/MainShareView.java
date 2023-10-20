package darling.ui.view;

import darling.domain.LastPrice;
import darling.domain.MainShare;

public record MainShareView(MainShare share, LastPrice lastPrice) {
}