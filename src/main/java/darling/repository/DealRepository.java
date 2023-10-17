package darling.repository;

import darling.domain.Deal;
import darling.domain.Operation;
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
import java.util.ArrayList;
import java.util.List;

public class DealRepository {

    public List<Deal> findAllOpenDeals() {
        List<Deal> deals = new ArrayList<>();
        String selectSql = "SELECT id, broker_account_id, parent_operation_id, name, date, type, description, state, " +
                "instrument_uid, instrument_type, payment, price, commission, operation.quantity, quantity_rest, " +
                "quantity_done, open_deal.quantity quantity_deal " +
                "FROM open_deal LEFT JOIN operation ON open_deal.operation_id = operation.id";
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    Operation operation = Operation.builder()
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
                    Deal deal = new Deal(operation, rs.getLong("quantity_deal"));
                    deals.add(deal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deals;
    }

    public void refreshOpenDeals(List<Deal> deals) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM open_deal WHERE true = true")) {
                    ps.executeUpdate();
                }

                String insertQueueSql = "INSERT INTO open_deal (operation_id, quantity) VALUES (?,?)";
                try (PreparedStatement ps = connection.prepareStatement(insertQueueSql)) {
                    for (Deal d : deals) {
                        ps.setString(1, d.getOpenOperationId());
                        ps.setLong(2, d.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}