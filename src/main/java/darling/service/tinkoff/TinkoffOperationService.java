package darling.service.tinkoff;

import darling.context.MarketContext;
import darling.context.event.Event;
import darling.context.event.EventListener;
import darling.domain.operations.model.Operation;
import darling.domain.operations.model.OperationMapper;
import darling.repository.OperationRepository;
import darling.service.OperationService;
import darling.shared.TinkoffTypeMapper;
import ru.tinkoff.piapi.contract.v1.GetOperationsByCursorResponse;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static darling.shared.ApplicationProperties.ACCOUNTS;

public class TinkoffOperationService implements OperationService {

    private final OperationRepository operationRepository = new OperationRepository();

    private boolean isStartedLoad = true;

    private final List<EventListener> listeners = new ArrayList<>();

    private static final ru.tinkoff.piapi.core.OperationsService operationsService = MarketContext.TINKOFF_CLIENT.getOperationsService();

    @Override
    public List<Operation> getAll() {
        return operationRepository.findAll();
    }

    /**
     * Синхронизирует операции с сервером.
     */
    @Override
    public void sync() {
        int count = 0;
        for (String account : ACCOUNTS) {
            Instant from = TinkoffTypeMapper.map(operationRepository.getLastTime().minusMinutes(10));
            Instant to = TinkoffTypeMapper.map(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
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
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
}