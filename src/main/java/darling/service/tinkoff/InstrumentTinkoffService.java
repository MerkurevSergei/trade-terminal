package darling.service.tinkoff;

import darling.domain.Share;
import darling.mapper.ShareMapper;
import darling.repository.AvailableShareRepository;
import darling.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;

import java.util.List;

@RequiredArgsConstructor
public class InstrumentTinkoffService implements InstrumentService {

    private final AvailableShareRepository availableShareRepository = new AvailableShareRepository();
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
}