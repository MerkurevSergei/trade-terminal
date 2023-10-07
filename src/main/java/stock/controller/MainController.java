package stock.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.Share;
import stock.repository.MainShareRepository;
import stock.shared.BeanRegister;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Getter
public class MainController implements Initializable {

    private final MainShareRepository mainShareRepository = BeanRegister.MAIN_SHARE_REPOSITORY;

    @FXML
    public ListView<Share> mainSharesView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainSharesView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Share t, boolean empty) {
                super.updateItem(t, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(t.getName());
                }
            }
        });
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public void openStockList(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/stocklist.fxml"));
        Parent stockListFxml = loader.load();
        StockListController controller = loader.getController();
        controller.setCallback(this::addToList);

        Stage childStage = new Stage();
        childStage.setTitle("Выбор акций");
        Scene scene = new Scene(stockListFxml);
        childStage.setScene(scene);
        childStage.initModality(Modality.APPLICATION_MODAL);

        childStage.show();
    }

    public void addToList(Share value) {
        mainShareRepository.save(value);
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public void deleteFromList(ActionEvent actionEvent) {
        Share selectedItem = mainSharesView.getSelectionModel().getSelectedItem();
        mainShareRepository.deleteById(selectedItem.getFigi());
        this.mainSharesView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }
}
