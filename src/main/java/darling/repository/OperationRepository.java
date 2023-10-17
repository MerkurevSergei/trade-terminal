package darling.repository;

import darling.domain.Operation;
import lombok.Synchronized;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.RoundingMode;
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
    public LocalDateTime getLastTime(String account) {
        LocalDateTime lastTime = LocalDateTime.of(2020, JANUARY, 1, 0, 0, 0);
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT max(date) date FROM operation where broker_account_id = ?")) {
                ps.setString(1, account);
                ResultSet rs = ps.executeQuery();
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
            try {
                connection.setAutoCommit(false);
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
                String insertSql = "INSERT INTO operation (id, broker_account_id, parent_operation_id, name, date, type, description, state, " +
                        "instrument_uid, instrument_type, payment, price, commission, quantity, quantity_rest, quantity_done) " +
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

                // Новые добавим в очередь новых для обработки процессом обработки операций
                String insertQueueSql = "INSERT INTO operation_queue (id, broker_account_id, parent_operation_id, name, date, type, description, state, " +
                        "instrument_uid, instrument_type, payment, price, commission, quantity, quantity_rest, quantity_done) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = connection.prepareStatement(insertQueueSql)) {
                    for (Operation o : operations) {
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
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Коммит
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
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
                 ResultSet rs = stmt.executeQuery("SELECT id, broker_account_id, parent_operation_id, name, date, type, description, state, instrument_uid, " +
                                                          "instrument_type, payment, price, commission, quantity, quantity_rest, quantity_done FROM operation ORDER BY date desc")) {
                while (rs.next()) {
                    Operation share = Operation.builder()
                            .id(rs.getString("id"))
                            .brokerAccountId(rs.getString("broker_account_id"))
                            .parentOperationId(rs.getString("parent_operation_id"))
                            .name(rs.getString("name"))
                            .date(rs.getTimestamp("date").toLocalDateTime())
                            .type(OperationType.valueOf(rs.getString("type")))
                            .description(rs.getString("description"))
                            .state(OperationState.valueOf(rs.getString("state")))
                            .instrumentUid(rs.getString("instrument_uid"))
                            .instrumentType(InstrumentType.valueOf(rs.getString("instrument_type")))
                            .payment(rs.getBigDecimal("payment").setScale(2, RoundingMode.HALF_UP))
                            .price(rs.getBigDecimal("price").setScale(6, RoundingMode.HALF_UP))
                            .commission(rs.getBigDecimal("commission").setScale(2, RoundingMode.HALF_UP))
                            .quantity(rs.getLong("quantity"))
                            .quantityRest(rs.getLong("quantity_rest"))
                            .quantityDone(rs.getLong("quantity_done"))
                            .build();
                    operations.add(share);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }

    public List<Operation> popFromQueue() {
        List<Operation> operations = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, broker_account_id, parent_operation_id, name, date, type, description, state, instrument_uid, " +
                                                          "instrument_type, payment, price, commission, quantity, quantity_rest, quantity_done FROM operation_queue ORDER BY date desc")) {
                while (rs.next()) {
                    Operation share = Operation.builder()
                            .id(rs.getString("id"))
                            .brokerAccountId(rs.getString("broker_account_id"))
                            .parentOperationId(rs.getString("parent_operation_id"))
                            .name(rs.getString("name"))
                            .date(rs.getTimestamp("date").toLocalDateTime())
                            .type(OperationType.valueOf(rs.getString("type")))
                            .description(rs.getString("description"))
                            .state(OperationState.valueOf(rs.getString("state")))
                            .instrumentUid(rs.getString("instrument_uid"))
                            .instrumentType(InstrumentType.valueOf(rs.getString("instrument_type")))
                            .payment(rs.getBigDecimal("payment").setScale(2, RoundingMode.HALF_UP))
                            .price(rs.getBigDecimal("price").setScale(6, RoundingMode.HALF_UP))
                            .commission(rs.getBigDecimal("commission").setScale(2, RoundingMode.HALF_UP))
                            .quantity(rs.getLong("quantity"))
                            .quantityRest(rs.getLong("quantity_rest"))
                            .quantityDone(rs.getLong("quantity_done"))
                            .build();
                    operations.add(share);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM operation_queue WHERE true = true")) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }
}
