package darling.service;

import darling.domain.Share;

import java.util.List;

public interface InstrumentService {
    void syncAvailableShares();
    List<Share> getAvailableShares();
}