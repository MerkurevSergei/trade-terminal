package darling.shared;

import darling.context.MarketContext;
import darling.ui.AbstractController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaFxUtils {
    public static FXMLLoader openWindow(String path, String title, MarketContext marketContext) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(JavaFxUtils.class.getResource(path));
        try {
            Parent stockListFxml = loader.load();
            AbstractController controller = loader.getController();
            controller.setContextAndInit(marketContext);

            Stage childStage = new Stage();
            childStage.setTitle(title);
            Scene scene = new Scene(stockListFxml);
            childStage.setScene(scene);
            childStage.initModality(Modality.APPLICATION_MODAL);
            childStage.show();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return loader;
    }
}
