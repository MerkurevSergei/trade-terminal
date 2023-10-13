package darling.service.tinkoff;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.Operation;
import darling.domain.Position;
import darling.mapper.OperationMapper;
import darling.mapper.PositionMapper;
import darling.mapper.TinkoffSpecialTypeMapper;
import darling.repository.OperationRepository;
import darling.repository.PositionRepository;
import darling.service.OperationService;
import ru.tinkoff.piapi.contract.v1.GetOperationsByCursorResponse;
import ru.tinkoff.piapi.core.models.Positions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static darling.shared.ApplicationProperties.ACCOUNTS;

public class OperationTinkoffService implements OperationService {

    private final OperationRepository operationRepository = new OperationRepository();

    private final PositionRepository positionRepository = new PositionRepository();

    private boolean isStartedLoad = true;

    private final List<EventListener> listeners = new ArrayList<>();

    private static final ru.tinkoff.piapi.core.OperationsService operationsService = MarketContext.TINKOFF_CLIENT.getOperationsService();

    @Override
    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    @Override
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    /**
     * Синхронизирует операции с сервером.
     */
    @Override
    public void syncOperations() {
        int count = 0;
        for (String account : ACCOUNTS) {
            Instant from = TinkoffSpecialTypeMapper.map(operationRepository.getLastTime().minusMinutes(10));
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

        if (isStartedLoad || count > 0) {
            isStartedLoad = false;
            listeners.forEach(it -> it.handle(Event.OPERATION_UPDATED));
        }
    }

    @Override
    public void syncPositions() {
        List<Position> positions = new ArrayList<>();
        for (String account : ACCOUNTS) {
            Positions tinkoffPositions = operationsService.getPositionsSync(account);
            List<Position> positionsByAccount = tinkoffPositions.getSecurities().stream().map(PositionMapper.INST::map).toList();
            positions.addAll(positionsByAccount);
        }
        positionRepository.saveAll(positions);
        listeners.forEach(it -> it.handle(Event.POSITION_UPDATED));
    }

    @Override
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
}