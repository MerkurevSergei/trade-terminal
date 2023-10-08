package stock.ui.trade;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import ru.tinkoff.piapi.contract.v1.Share;
import stock.repository.MainShareRepository;

public record ShareListManager(ListView<Share> shareListView, MainShareRepository mainShareRepository) {

    public ShareListManager(ListView<Share> shareListView, MainShareRepository mainShareRepository) {
        this.mainShareRepository = mainShareRepository;
        this.shareListView = shareListView;
        init();
    }

    void init() {
        shareListView.setCellFactory(param -> new ListCell<>() {
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
        shareListView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public void addShare(Share share) {
        mainShareRepository.save(share);
        shareListView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public void deleteActiveShare() {
        Share selectedItem = shareListView.getSelectionModel().getSelectedItem();
        mainShareRepository.deleteById(selectedItem.getFigi());
        shareListView.getItems().setAll(mainShareRepository.getSharesAndSort());
    }

    public Share getSelectedItem() {
        return shareListView.getSelectionModel().getSelectedItem();
    }

}
