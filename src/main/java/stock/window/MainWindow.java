package stock.window;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainWindow extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/main.fxml");
        loader.setLocation(xmlUrl);
        Parent root = loader.load();

        stage.setTitle(new String("Моя прелесть".getBytes("WINDOWS-1251"), StandardCharsets.UTF_8));
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
