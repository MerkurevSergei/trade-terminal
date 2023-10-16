package darling.service.tinkoff;

import darling.domain.Share;
import darling.mapper.ShareMapper;
import darling.repository.AvailableShareRepository;
import darling.repository.MainShareRepository;
import darling.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InstrumentTinkoffService implements InstrumentService {

    private final AvailableShareRepository availableShareRepository = new AvailableShareRepository();
    private final MainShareRepository mainShareRepository = new MainShareRepository();
    private final ru.tinkoff.piapi.core.InstrumentsService instrumentsService;

    @Override
    public void syncAvailableShares() {
        availableShareRepository.deleteAll();
        List<Share> shares = ShareMapper.INST.map(instrumentsService.getSharesSync(InstrumentStatus.INSTRUMENT_STATUS_BASE));
        availableShareRepository.saveAll(shares);
    }

    @Override
    public List<Share> getAvailableShares() {
        return availableShareRepository.findAll();
    }

    @Override
    public Map<String, Share> getAvailableSharesDict() {
        return availableShareRepository.findAll().stream().collect(Collectors.toMap(Share::uid, Function.identity()));
    }

    @Override
    public void addMainShare(Share share) {
        mainShareRepository.save(share);
    }

    @Override
    public void deleteMainShare(Share share) {
        mainShareRepository.deleteById(share.uid());
    }

    @Override
    public List<Share> getMainShares() {
        return mainShareRepository.getSharesAndSort();
    }
}