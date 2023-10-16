package darling.service;

import darling.domain.Share;

import java.util.List;
import java.util.Map;

public interface InstrumentService {

    List<Share> getAvailableShares();

    Map<String, Share> getAvailableSharesDict();

    void syncAvailableShares();

    List<Share> getMainShares();

    void addMainShare(Share share);

    void deleteMainShare(Share share);
}