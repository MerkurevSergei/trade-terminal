package darling.repository.db;

import darling.domain.MainShare;
import lombok.Synchronized;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainShareDbRepository {

    @Synchronized
    public void save(MainShare share) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            String insertSql = "INSERT INTO main_share (uid, figi, ticker, name, lot, is_trade) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, share.uid());
                ps.setString(2, share.figi());
                ps.setString(3, share.ticker());
                ps.setString(4, share.name());
                ps.setInt(5, share.lot());
                ps.setBoolean(6, share.isTrade());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Synchronized
    public List<MainShare> getSharesAndSort() {
        ArrayList<MainShare> shares = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uid, figi, ticker, name, lot, is_trade FROM main_share")) {
                while (rs.next()) {
                    MainShare share = MainShare.builder()
                            .uid(rs.getString("uid"))
                            .figi(rs.getString("figi"))
                            .ticker(rs.getString("ticker"))
                            .name(rs.getString("name"))
                            .lot(rs.getInt("lot"))
                            .isTrade(rs.getBoolean("is_trade"))
                            .build();
                    shares.add(share);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        shares.sort(Comparator.comparing(MainShare::name));
        return shares;
    }

    @Synchronized
    public void deleteById(String uid) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM main_share WHERE uid = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
