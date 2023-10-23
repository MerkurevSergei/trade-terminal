package darling.service.sand;

import darling.domain.MainShare;
import darling.domain.Share;
import darling.mapper.ShareMapper;
import darling.repository.db.AvailableShareDbRepository;
import darling.repository.db.MainShareDbRepository;
import darling.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InstrumentSandService implements InstrumentService {

    private final AvailableShareDbRepository availableShareDbRepository = new AvailableShareDbRepository();
    private final MainShareDbRepository mainShareDbRepository = new MainShareDbRepository();
    private final ru.tinkoff.piapi.core.InstrumentsService instrumentsService;

    @Override
    public void syncAvailableShares() {
        availableShareDbRepository.deleteAll();
        List<Share> shares = ShareMapper.INST.map(instrumentsService.getSharesSync(InstrumentStatus.INSTRUMENT_STATUS_BASE));
        availableShareDbRepository.saveAll(shares);
    }

    @Override
    public List<Share> getAvailableShares() {
        return availableShareDbRepository.findAll();
    }

    @Override
    public Map<String, Share> getAvailableSharesDict() {
        return availableShareDbRepository.findAll().stream().collect(Collectors.toMap(Share::uid, Function.identity()));
    }

    @Override
    public void addMainShare(MainShare share) {
        mainShareDbRepository.save(share);
    }

    @Override
    public void deleteMainShare(MainShare share) {
        mainShareDbRepository.deleteById(share.uid());
    }

    @Override
    public List<MainShare> getMainShares() {
        return mainShareDbRepository.getSharesAndSort();
    }
}