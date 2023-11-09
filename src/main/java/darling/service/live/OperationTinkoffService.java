package darling.service.live;

import darling.domain.Operation;
import darling.domain.Position;
import darling.domain.Share;
import darling.mapper.OperationMapper;
import darling.mapper.PositionMapper;
import darling.mapper.TinkoffSpecialTypeMapper;
import darling.repository.OperationRepository;
import darling.repository.db.AvailableShareDbRepository;
import darling.repository.memory.PositionMemoryRepository;
import darling.service.OperationService;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.GetOperationsByCursorResponse;
import ru.tinkoff.piapi.core.models.Positions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static darling.shared.ApplicationProperties.ACCOUNTS;

@RequiredArgsConstructor
public class OperationTinkoffService implements OperationService {

    private final AvailableShareDbRepository availableShareDbRepository;
    private final OperationRepository operationRepository;
    private final PositionMemoryRepository positionMemoryRepository;
    private final ru.tinkoff.piapi.core.OperationsService operationsService;

    @Override
    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    /**
     * Синхронизирует операции с сервером.
     */
    @Override
    public boolean syncOperations() {
        int count = 0;
        for (String account : ACCOUNTS) {
            Instant from = TinkoffSpecialTypeMapper.map(operationRepository.getLastOperationTime(account).minusMinutes(4320));
            Instant to = TinkoffSpecialTypeMapper.map(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
            GetOperationsByCursorResponse cursor = operationsService.getOperationByCursorSync(account, from, to);
            List<Operation> operationPart = cursor.getItemsList().stream().map(OperationMapper.INST::map).toList();
            List<Operation> operations = new ArrayList<>(operationPart);
            while (cursor.getHasNext()) {
                cursor = operationsService.getOperationByCursorSync(account, from, to, cursor.getNextCursor(), null, null, null,
                                                                    false, false, false, null);
                operations.addAll(cursor.getItemsList().stream().map(OperationMapper.INST::map).toList());
            }
            count = count + operationRepository.saveNew(operations);
        }
        return count > 0;
    }

    @Override
    public List<Position> getAllPositions() {
        return positionMemoryRepository.findAll();
    }

    @Override
    public void syncPositions() {
        List<Position> positions = new ArrayList<>();
        for (String account : ACCOUNTS) {
            Positions tinkoffPositions = operationsService.getPositionsSync(account);
            List<Position> positionsByAccount = tinkoffPositions.getSecurities().stream().map(PositionMapper.INST::map).toList();
            positions.addAll(positionsByAccount);
        }
        enrichPositions(positions);
        positionMemoryRepository.saveAll(positions);
    }

    private void enrichPositions(List<Position> positions) {
        Map<String, Share> shareByFigi = availableShareDbRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Share::figi, Function.identity()));
        for (Position position: positions) {
            Share share = shareByFigi.get(position.figi());
            if (share == null) continue;
            position.setInstrumentUid(share.uid());
            position.setName(share.name());
            position.setTicker(share.ticker());
            position.setLotBalance(position.balance() / share.lot());
        }

    }
}