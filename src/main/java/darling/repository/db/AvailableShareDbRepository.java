package darling.repository.db;

import darling.domain.Share;
import lombok.Synchronized;
import ru.tinkoff.piapi.contract.v1.RealExchange;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AvailableShareDbRepository {

    @Synchronized
    public List<Share> findAll() {
        ArrayList<Share> shares = new ArrayList<>();
        String selectSql = "SELECT uid, figi, ticker, name, lot, currency, exchange, real_exchange, short_enabled_flag, " +
                "country_of_risk, sector, class_code, share_type, first_1min_candle_date, first_1day_candle_date " +
                "FROM available_share";
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    LocalDateTime first1MinCandleDate = rs.getTimestamp("first_1min_candle_date") != null ?  rs.getTimestamp("first_1min_candle_date").toLocalDateTime() : null;
                    LocalDateTime first1DayCandleDate = rs.getTimestamp("first_1day_candle_date") != null ?  rs.getTimestamp("first_1day_candle_date").toLocalDateTime() : null;
                    Share share = Share.builder()
                            .uid(rs.getString("uid"))
                            .figi(rs.getString("figi"))
                            .ticker(rs.getString("ticker"))
                            .name(rs.getString("name"))
                            .lot(rs.getInt("lot"))
                            .currency(rs.getString("currency"))
                            .exchange(rs.getString("exchange"))
                            .realExchange(RealExchange.valueOf(rs.getString("real_exchange")))
                            .shortEnabledFlag(rs.getBoolean("short_enabled_flag"))
                            .countryOfRisk(rs.getString("country_of_risk"))
                            .sector(rs.getString("sector"))
                            .classCode(rs.getString("class_code"))
                            .shareType(rs.getString("share_type"))
                            .first1MinCandleDate(first1MinCandleDate)
                            .first1DayCandleDate(first1DayCandleDate)
                            .build();
                    shares.add(share);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        shares.sort(Comparator.comparing(Share::ticker));
        return shares;
    }

    @Synchronized
    public void saveAll(List<Share> shares) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            String insertSql = "INSERT INTO available_share (uid, figi, ticker, name, lot, currency, exchange, real_exchange, " +
                    "short_enabled_flag, country_of_risk, sector, class_code, share_type, first_1min_candle_date, first_1day_candle_date) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                for (Share share : shares) {
                    ps.setString(1, share.uid());
                    ps.setString(2, share.figi());
                    ps.setString(3, share.ticker());
                    ps.setString(4, share.name());
                    ps.setInt(5, share.lot());
                    ps.setString(6, share.currency());
                    ps.setString(7, share.exchange());
                    ps.setString(8, share.realExchange().toString());
                    ps.setBoolean(9, share.shortEnabledFlag());
                    ps.setString(10, share.countryOfRisk());
                    ps.setString(11, share.sector());
                    ps.setString(12, share.classCode());
                    ps.setString(13, share.shareType());
                    Timestamp first1MinCandleDate = share.first1MinCandleDate() != null ?  Timestamp.valueOf(share.first1MinCandleDate()) : null;
                    Timestamp first1DayCandleDate = share.first1DayCandleDate() != null ?  Timestamp.valueOf(share.first1DayCandleDate()) : null;
                    ps.setTimestamp(14, first1MinCandleDate);
                    ps.setTimestamp(15, first1DayCandleDate);
                    ps.addBatch();
                    ps.clearParameters();
                }
                ps.executeBatch();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Synchronized
    public void deleteAll() {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM available_share WHERE true = true")) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}