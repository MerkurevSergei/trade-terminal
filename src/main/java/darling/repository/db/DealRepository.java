package darling.repository.db;

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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DealRepository {

    public List<Deal> findAllOpenDeals() {
        List<Deal> deals = new ArrayList<>();
        String selectSql = "SELECT id, broker_account_id, parent_operation_id, name, date, type, description, state, " +
                "instrument_uid, instrument_type, payment, price, commission, operation.quantity, quantity_rest, " +
                "quantity_done, open_deal.take_profit_price, open_deal.quantity quantity_deal " +
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
                    Deal deal = new Deal(operation, rs.getLong("quantity_deal"), rs.getBigDecimal("take_profit_price"));
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

                String insertQueueSql = "INSERT INTO open_deal (operation_id, quantity, take_profit_price) VALUES (?,?,?)";
                try (PreparedStatement ps = connection.prepareStatement(insertQueueSql)) {
                    for (Deal d : deals) {
                        ps.setString(1, d.getOpenOperationId());
                        ps.setLong(2, d.getQuantity());
                        ps.setBigDecimal(3, d.getTakeProfitPrice());
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

    public List<Deal> getClosedDeals(LocalDateTime start, LocalDateTime end) {
        List<Deal> deals = new ArrayList<>();
        String selectSql = "SELECT cd.id cd_id, cd.quantity cd_quantity, cd.take_profit_price cd_take_profit_price, " +
                "oop.id oop_id, oop.broker_account_id oop_broker_account_id, oop.parent_operation_id oop_parent_operation_id, oop.name oop_name, " +
                "oop.date oop_date, oop.type oop_type, oop.description oop_description, oop.state oop_state, oop.instrument_uid oop_instrument_uid, " +
                "oop.instrument_type oop_instrument_type, oop.payment oop_payment, oop.price oop_price, oop.commission oop_commission, oop.quantity oop_quantity, " +
                "oop.quantity_rest oop_quantity_rest, oop.quantity_done oop_quantity_done, " +
                "cop.id cop_id, cop.broker_account_id cop_broker_account_id, cop.parent_operation_id cop_parent_operation_id, cop.name cop_name, cop.date cop_date, cop.type cop_type, cop.description cop_description, " +
                "cop.state cop_state, cop.instrument_uid cop_instrument_uid, cop.instrument_type cop_instrument_type, cop.payment cop_payment, cop.price cop_price, " +
                "cop.commission cop_commission, cop.quantity cop_quantity, cop.quantity_rest cop_quantity_rest, cop.quantity_done  cop_quantity_done " +
                "FROM closed_deal cd " +
                "LEFT JOIN operation oop ON cd.open_operation_id = oop .id " +
                "LEFT JOIN operation cop ON cd.close_operation_id = cop .id " +
                "WHERE close_date BETWEEN ? AND ?";
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Operation openOperation = Operation.builder()
                            .id(rs.getString("oop_id"))
                            .brokerAccountId(rs.getString("oop_broker_account_id"))
                            .parentOperationId(rs.getString("oop_parent_operation_id"))
                            .name(rs.getString("oop_name"))
                            .date(rs.getTimestamp("oop_date").toLocalDateTime())
                            .type(OperationType.valueOf(rs.getString("oop_type")))
                            .description(rs.getString("oop_description"))
                            .state(OperationState.valueOf(rs.getString("oop_state")))
                            .instrumentUid(rs.getString("oop_instrument_uid"))
                            .instrumentType(InstrumentType.valueOf(rs.getString("oop_instrument_type")))
                            .payment(rs.getBigDecimal("oop_payment").setScale(9, RoundingMode.HALF_UP))
                            .price(rs.getBigDecimal("oop_price").setScale(9, RoundingMode.HALF_UP))
                            .commission(rs.getBigDecimal("oop_commission").setScale(9, RoundingMode.HALF_UP))
                            .quantity(rs.getLong("oop_quantity"))
                            .quantityRest(rs.getLong("oop_quantity_rest"))
                            .quantityDone(rs.getLong("oop_quantity_done"))
                            .build();
                    Operation closeOperation = Operation.builder()
                            .id(rs.getString("cop_id"))
                            .brokerAccountId(rs.getString("cop_broker_account_id"))
                            .parentOperationId(rs.getString("cop_parent_operation_id"))
                            .name(rs.getString("cop_name"))
                            .date(rs.getTimestamp("cop_date").toLocalDateTime())
                            .type(OperationType.valueOf(rs.getString("cop_type")))
                            .description(rs.getString("cop_description"))
                            .state(OperationState.valueOf(rs.getString("cop_state")))
                            .instrumentUid(rs.getString("cop_instrument_uid"))
                            .instrumentType(InstrumentType.valueOf(rs.getString("cop_instrument_type")))
                            .payment(rs.getBigDecimal("cop_payment").setScale(9, RoundingMode.HALF_UP))
                            .price(rs.getBigDecimal("cop_price").setScale(9, RoundingMode.HALF_UP))
                            .commission(rs.getBigDecimal("cop_commission").setScale(9, RoundingMode.HALF_UP))
                            .quantity(rs.getLong("cop_quantity"))
                            .quantityRest(rs.getLong("cop_quantity_rest"))
                            .quantityDone(rs.getLong("cop_quantity_done"))
                            .build();
                    Deal deal = new Deal(openOperation, closeOperation, rs.getLong("cd_quantity"), rs.getBigDecimal("cd_take_profit_price"));
                    deals.add(deal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deals;
    }

    public void saveClosedDeals(List<Deal> closedDeals) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            String insertQueueSql = "INSERT INTO closed_deal (id, open_operation_id, close_operation_id, open_date, close_date, quantity, take_profit_price) " +
                    "VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertQueueSql)) {
                for (Deal d : closedDeals) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, d.getOpenOperationId());
                    ps.setString(3, d.getCloseOperationId());
                    ps.setTimestamp(4, Timestamp.valueOf(d.getOpenDate()));
                    ps.setTimestamp(5, Timestamp.valueOf(d.getCloseDate()));
                    ps.setLong(6, d.getQuantity());
                    ps.setBigDecimal(7, d.getTakeProfitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}