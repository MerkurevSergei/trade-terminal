package darling.ui;

import darling.context.MarketContext;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractController implements Initializable {

    protected MarketContext marketContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public abstract void setContextAndInit(MarketContext marketContext);
}