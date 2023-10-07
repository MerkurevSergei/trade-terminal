package stock.controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class MainController extends Application {

    @FXML
    public ListView<String> mainSharesView;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        stage.setTitle(new String("Моя прелесть".getBytes("WINDOWS-1251"), StandardCharsets.UTF_8));
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void openStockList(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/stocklist.fxml"));
        Parent stockListFxml = loader.load();
        StockListController controller = loader.getController();
        controller.setCallback(this::addToList);

        Stage childStage = new Stage();
        childStage.setTitle(new String("Выбор акций".getBytes("WINDOWS-1251"), StandardCharsets.UTF_8));
        Scene scene = new Scene(stockListFxml);
        childStage.setScene(scene);
        childStage.initModality(Modality.APPLICATION_MODAL);

        childStage.show();


    }

    public void addToList(String value) {

        this.mainSharesView.getItems().add(value);

    }

    public static void main(String[] args) {
        launch();
    }
}
