package darling.domain.positions;

import darling.context.SandMarketContext;
import darling.context.event.EventListener;
import darling.domain.positions.model.Operation;
import darling.domain.positions.model.OperationMapper;
import darling.shared.TinkoffTypeMapper;
import ru.tinkoff.piapi.contract.v1.GetOperationsByCursorResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static darling.shared.ApplicationProperties.ACCOUNT_ID;

public class TinkoffOperationsService implements OperationsService {

    private final List<EventListener> listeners = new ArrayList<>();

    private static final ru.tinkoff.piapi.core.OperationsService operationsService = SandMarketContext.TINKOFF_CLIENT.getOperationsService();

    @Override
    public void refreshOperations(LocalDateTime start, LocalDateTime end) {
        Instant from = TinkoffTypeMapper.map(start);
        Instant to = TinkoffTypeMapper.map(end);
        GetOperationsByCursorResponse cursor = operationsService.getOperationByCursorSync(ACCOUNT_ID, from, to);
        List<Operation> operationPart = cursor.getItemsList().stream().map(OperationMapper.INST::map).toList();

        List<Operation> operations = new ArrayList<>(operationPart);
        while (cursor.getHasNext()) {
            operationsService.getOperationByCursorSync(ACCOUNT_ID, from, to, cursor.getNextCursor(), null, null, null,
                                                       false, false, false, null);
            operations.addAll(cursor.getItemsList().stream().map(OperationMapper.INST::map).toList());
        }
        listeners.forEach(it -> it.handle(operations));
    }

    @Override
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
}