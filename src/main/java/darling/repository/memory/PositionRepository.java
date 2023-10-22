package darling.repository.memory;

import darling.domain.Position;

import java.util.ArrayList;
import java.util.List;

public class PositionRepository {

    private final List<Position> positions = new ArrayList<>();

    public void saveAll(List<Position> newPositions) {
        positions.clear();
        positions.addAll(newPositions);
    }

    public List<Position> findAll() {
        return new ArrayList<>(positions);
    }

}