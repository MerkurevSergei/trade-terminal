package stocks.ui.trade;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import ru.tinkoff.piapi.contract.v1.Share;
import stocks.repository.ShareRepository;

public record ShareListManager(ListView<Share> shareListView, ShareRepository shareRepository) {

    public ShareListManager(ListView<Share> shareListView, ShareRepository shareRepository) {
        this.shareRepository = shareRepository;
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
        shareListView.getItems().setAll(shareRepository.getSharesAndSort());
    }

    public void addShare(Share share) {
        shareRepository.save(share);
        shareListView.getItems().setAll(shareRepository.getSharesAndSort());
    }

    public void deleteActiveShare() {
        Share selectedItem = shareListView.getSelectionModel().getSelectedItem();
        shareRepository.deleteById(selectedItem.getFigi());
        shareListView.getItems().setAll(shareRepository.getSharesAndSort());
    }

    public Share getSelectedItem() {
        return shareListView.getSelectionModel().getSelectedItem();
    }

}
