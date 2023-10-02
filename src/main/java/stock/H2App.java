package stock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2App {
    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./data/darling", "sa", "");
        conn.close();
    }
}
