package dto;

import java.sql.*;

public class DB_Connection {
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException{
        if (conn == null || conn.isClosed()){
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres","uninaswap" );
        }
        return conn;
    }
}