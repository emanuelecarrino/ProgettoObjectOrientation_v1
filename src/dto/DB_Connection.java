package dto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB_Connection {
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver not found on classpath", e);
            }



            String url = "jdbc:postgresql://localhost:5432/postgres?currentSchema=uninaswap";
            String user = "postgres";
            String password = "uninaswap";
            conn = DriverManager.getConnection(url, user, password);

            try (Statement st = conn.createStatement()) {
                st.execute("SET search_path TO uninaswap, public");
            }
        }
        return conn;
    }
}