package darling.ui.main;

import darling.context.MarketContext;
import darling.domain.Share;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public record MainShareManager(TableView<Share> mainSharesTableView, MarketContext marketContext) {

    public MainShareManager {
        TableColumn<Share, String> tableColumnTiker = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(0);
        TableColumn<Share, String> tableColumnName = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(1);
        TableColumn<Share, String> tableColumnLot = (TableColumn<Share, String>) mainSharesTableView.getColumns().get(2);
        tableColumnTiker.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().ticker()));
        tableColumnName.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().name()));
        tableColumnLot.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().lot().toString()));
    }

//    void init() {
//        shareListView.setCellFactory(param -> new ListCell<>() {
//            @Override
//            protected void updateItem(Share t, boolean empty) {
//                super.updateItem(t, empty);
//                if (empty) {
//                    setText(null);
//                } else {
//                    setText(t.getName());
//                }
//            }
//        });
//        shareListView.getItems().setAll(availableShareRepository.findAll());
//    }
//
//    public void addShare(darling.domain.Share share) {
//
//        availableShareRepository.save(share);
//        shareListView.getItems().setAll(availableShareRepository.findAll());
//    }
//
//    public void deleteActiveShare() {
//        Share selectedItem = shareListView.getSelectionModel().getSelectedItem();
//        availableShareRepository.deleteById(selectedItem.getFigi());
//        shareListView.getItems().setAll(availableShareRepository.findAll());
//    }
//
//    public Share getSelectedItem() {
//        return shareListView.getSelectionModel().getSelectedItem();
//    }

}
