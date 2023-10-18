package darling.ui.view;

import darling.domain.LastPrice;
import darling.domain.Share;

public record MainShareView(Share share, LastPrice lastPrice) {
}