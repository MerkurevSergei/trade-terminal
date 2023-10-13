package darling.repository;

import darling.domain.operations.model.Operation;
import lombok.Synchronized;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.RoundingMode;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.Month.JANUARY;

public class OperationRepository {

    @Synchronized
    public LocalDateTime getLastTime() {
        LocalDateTime lastTime = LocalDateTime.of(2020, JANUARY, 1, 0, 0, 0);
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT max(date) date FROM operation")) {
                if (rs.next()) {
                    Timestamp date = rs.getTimestamp("date");
                    return date == null ? lastTime : date.toLocalDateTime();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastTime;
    }

    @Synchronized
    public int saveNew(List<Operation> operations) {
        List<String> newIds = operations.stream().map(Operation::id).toList();
        List<String> existIds = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {

            // Если новые операции уже были сохранены в БД, проверим это
            if (!newIds.isEmpty()) {
                String selectExisting = "SELECT id FROM operation WHERE id in (%s)";
                selectExisting = String.format(selectExisting, String.join(",", Collections.nCopies(newIds.size(), "?")));
                try (PreparedStatement ps = connection.prepareStatement(selectExisting)) {
                    for (int i = 0; i < newIds.size(); i++) {
                        ps.setObject(i + 1, newIds.get(i));
                    }
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String id = rs.getString("id");
                        existIds.add(id);
                    }
                }
            }

            // Удалим их, если существуют и сохраним только новые
            operations.removeIf(operation -> existIds.contains(operation.id()));
            String insertSql = "INSERT INTO operation (id, brokerAccountId, parentOperationId, name, date, type, description, state, " +
                    "instrumentUid, instrumentType, payment, price, commission, quantity, quantityRest, quantityDone) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            for (Operation o : operations) {
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, o.id());
                    ps.setString(2, o.brokerAccountId());
                    ps.setString(3, o.parentOperationId());
                    ps.setString(4, o.name());
                    ps.setTimestamp(5, java.sql.Timestamp.valueOf(o.date()));
                    ps.setString(6, o.type().toString());
                    ps.setString(7, o.description());
                    ps.setString(8, o.state().toString());
                    ps.setString(9, o.instrumentUid());
                    ps.setString(10, o.instrumentType().toString());
                    ps.setBigDecimal(11, o.payment());
                    ps.setBigDecimal(12, o.price());
                    ps.setBigDecimal(13, o.commission());
                    ps.setLong(14, o.quantity());
                    ps.setLong(15, o.quantityRest());
                    ps.setLong(16, o.quantityDone());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newIds.size() - existIds.size();
    }

    public List<Operation> findAll() {
        List<Operation> operations = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, brokerAccountId, parentOperationId, name, date, type, description, state, instrumentUid, " +
                                                          "instrumentType, payment, price, commission, quantity, quantityRest, quantityDone FROM operation ORDER BY date desc")) {
                while (rs.next()) {
                    Operation share = Operation.builder()
                            .id(rs.getString("id"))
                            .brokerAccountId(rs.getString("brokerAccountId"))
                            .parentOperationId(rs.getString("parentOperationId"))
                            .name(rs.getString("name"))
                            .date(rs.getTimestamp("date").toLocalDateTime())
                            .type(OperationType.valueOf(rs.getString("type")))
                            .description(rs.getString("description"))
                            .state(OperationState.valueOf(rs.getString("state")))
                            .instrumentUid(rs.getString("instrumentUid"))
                            .instrumentType(InstrumentType.valueOf(rs.getString("instrumentType")))
                            .payment(rs.getBigDecimal("payment").setScale(2, RoundingMode.HALF_UP))
                            .price(rs.getBigDecimal("price").setScale(6, RoundingMode.HALF_UP))
                            .commission(rs.getBigDecimal("commission").setScale(2, RoundingMode.HALF_UP))
                            .quantity(rs.getLong("quantity"))
                            .quantityRest(rs.getLong("quantityRest"))
                            .quantityDone(rs.getLong("quantityDone"))
                            .build();
                    operations.add(share);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }
}