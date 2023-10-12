package darling.repository;

import lombok.Synchronized;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static java.time.Month.JANUARY;

public class OperationRepository {

    @Synchronized
    public LocalDateTime getLastTime() {
        LocalDateTime lastTime = LocalDateTime.of(2020, JANUARY, 1, 0, 0, 0);
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "")) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT max(date) FROM operation")) {
                if (rs.next()) {
                    return rs.getTimestamp("date").toLocalDateTime();
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastTime;
    }
}
