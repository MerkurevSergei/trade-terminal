package darling.service.sand;

import darling.domain.Operation;
import darling.domain.Position;
import darling.repository.OperationRepository;
import darling.service.OperationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OperationSandService implements OperationService {

    private final OperationRepository operationRepository;

    @Override
    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    @Override
    public boolean syncOperations() {
        return true;
    }

    @Override
    public List<Position> getAllPositions() {
        return List.of();
    }

    @Override
    public void syncPositions() {
    }
}
