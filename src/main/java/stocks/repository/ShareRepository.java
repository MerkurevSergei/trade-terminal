package stocks.repository;

import com.google.protobuf.Timestamp;
import lombok.Synchronized;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShareRepository {

    @Synchronized
    public void save(Share share) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            String insertSql = "INSERT INTO share (figi, ticker, class_code, isin, lot, currency, short_enabled_flag, " +
                    "name, exchange, country_of_risk, sector, uid, real_exchange, position_uid, first_1min_candle_date, first_1day_candle_date) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, share.getFigi());
                ps.setString(2, share.getTicker());
                ps.setString(3, share.getClassCode());
                ps.setString(4, share.getIsin());
                ps.setInt(5, share.getLot());
                ps.setString(6, share.getCurrency());
                ps.setBoolean(7, share.getShortEnabledFlag());
                ps.setString(8, share.getName());
                ps.setString(9, share.getExchange());
                ps.setString(10, share.getCountryOfRisk());
                ps.setString(11, share.getSector());
                ps.setString(12, share.getUid());
                ps.setString(13, share.getRealExchange().toString());
                ps.setString(14, share.getPositionUid());
                Timestamp firstMin = share.getFirst1MinCandleDate();
                LocalDateTime fistMinLD = Instant.ofEpochSecond(firstMin.getSeconds(), firstMin.getNanos())
                        .atOffset(ZoneOffset.UTC)
                        .toLocalDateTime();
                Timestamp firstDay = share.getFirst1DayCandleDate();
                LocalDateTime fistDayLD = Instant.ofEpochSecond(firstDay.getSeconds(), firstDay.getNanos())
                        .atOffset(ZoneOffset.UTC)
                        .toLocalDateTime();
                ps.setString(15, fistMinLD.toString());
                ps.setString(16, fistDayLD.toString());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Synchronized
    public List<Share> getSharesAndSort() {
        ArrayList<Share> shares = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT figi, ticker, class_code, isin, lot, currency, short_enabled_flag, " +
                                                          "name, exchange, country_of_risk, sector, uid, real_exchange, position_uid, " +
                                                          "first_1min_candle_date, first_1day_candle_date FROM share")) {
                while (rs.next()) {
                    Share share = Share.newBuilder()
                            .setFigi(rs.getString("figi"))
                            .setTicker(rs.getString("ticker"))
                            .setClassCode(rs.getString("class_code"))
                            .setIsin(rs.getString("isin"))
                            .setLot(rs.getInt("lot"))
                            .setCurrency(rs.getString("currency"))
                            .setShortEnabledFlag(rs.getBoolean("short_enabled_flag"))
                            .setName(rs.getString("name"))
                            .setExchange(rs.getString("exchange"))
                            .setCountryOfRisk(rs.getString("country_of_risk"))
                            .setSector(rs.getString("sector"))
                            .setUid(rs.getString("uid"))
                            .setRealExchange(RealExchange.valueOf(rs.getString("real_exchange")))
                            .setPositionUid(rs.getString("position_uid"))
                            .build();
                    shares.add(share);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        shares.sort(Comparator.comparing(Share::getName));
        return shares;
    }

    @Synchronized
    public void deleteById(String figi) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM share WHERE figi = ?")) {
                ps.setString(1, figi);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
