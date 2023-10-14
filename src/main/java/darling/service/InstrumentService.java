package darling.service;

import darling.domain.Share;

import java.util.List;

public interface InstrumentService {

    List<Share> getAvailableShares();

    void syncAvailableShares();

    List<Share> getMainShares();

    void addMainShare(Share share);

    void deleteMainShare(Share share);
}