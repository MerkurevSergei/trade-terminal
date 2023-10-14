package darling.repository;

import darling.domain.Share;
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

public class MainShareRepository {

    @Synchronized
    public void save(Share share) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            String insertSql = "INSERT INTO main_share (uid, figi, ticker, name, lot) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, share.uid());
                ps.setString(2, share.figi());
                ps.setString(3, share.ticker());
                ps.setString(4, share.name());
                ps.setInt(5, share.lot());
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
                 ResultSet rs = stmt.executeQuery("SELECT uid, figi, ticker, name, lot FROM main_share")) {
                while (rs.next()) {
                    Share share = Share.builder()
                            .uid(rs.getString("uid"))
                            .figi(rs.getString("figi"))
                            .ticker(rs.getString("ticker"))
                            .name(rs.getString("name"))
                            .lot(rs.getInt("lot"))
                            .build();
                    shares.add(share);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        shares.sort(Comparator.comparing(Share::name));
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
