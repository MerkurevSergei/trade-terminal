package darling.service.sand;

import darling.domain.Operation;
import darling.domain.Position;
import darling.service.OperationService;

import java.util.List;

public class OperationSandService implements OperationService {

    @Override
    public List<Operation> getAllOperations() {
        return List.of();
    }

    @Override
    public List<Position> getAllPositions() {
        return List.of();
    }

    @Override
    public boolean syncOperations() {
        return true;
    }

    @Override
    public void syncPositions() {
    }
}
