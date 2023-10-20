package darling.service;

import darling.domain.MainShare;
import darling.domain.Share;

import java.util.List;
import java.util.Map;

public interface InstrumentService {

    List<Share> getAvailableShares();

    Map<String, Share> getAvailableSharesDict();

    void syncAvailableShares();

    List<MainShare> getMainShares();

    void addMainShare(MainShare share);

    void deleteMainShare(MainShare share);
}