package dto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DB_Connection{
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if(conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/uninaswap", "user", "password");
        }
        return conn;
    }
}