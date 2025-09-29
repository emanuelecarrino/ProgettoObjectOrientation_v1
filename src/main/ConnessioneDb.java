package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

public class ConnessioneDb {
    private Connection dbConnect = null;
    private Statement statement = null;
    private ResultSet result = null;
    private String databaseBootstrap = "postgres1"; // database da creare
    private String database = "postgres1";          // database da usare
    private String url = "jdbc:postgresql://localhost:5432/";
    private String user = "postgres";
    private String password = "password";
    private static ConnessioneDb instance = null;

    private ConnessioneDb() {}

    public static ConnessioneDb getInstance() {
        if (instance == null) {
            instance = new ConnessioneDb();
        }
        return instance;
    }

    public void generaDatabase() {
        try {
            dbConnect = DriverManager.getConnection(url, user, password);
            statement = dbConnect.createStatement();
            String query = "CREATE DATABASE " + databaseBootstrap;
            statement.executeUpdate(query);
            statement.close();
            dbConnect.close();
            dbConnect = DriverManager.getConnection(url + databaseBootstrap, user, password);
            statement = dbConnect.createStatement();
            eseguiScript("src/Main/define_tables.txt");
            eseguiScript("src/Main/popolamentodb1.txt");
            eseguiScript("src/Main/popolamentodb2.txt");
            eseguiScript("src/Main/popolamentodb3.txt");
            eseguiScript("src/Main/popolamentodb4.txt");
        } catch (SQLException | FileNotFoundException e) {
            if (e.getMessage() != null && e.getMessage().contains("esiste")) {
                // database già esistente: ignora
            } else {
                e.printStackTrace();
            }
        } finally {
            dbClose(result, statement, dbConnect);
        }
    }

    private void eseguiScript(String path) throws FileNotFoundException, SQLException {
        File file = new File(path);
        if (!file.exists()) return; // silenzioso se non esiste
        Scanner reader = new Scanner(file);
        StringBuilder data = new StringBuilder();
        while (reader.hasNextLine()) {
            data.append(reader.nextLine());
        }
        reader.close();
        statement.execute(data.toString());
    }

    public void dbConnect() {
        try {
            Class.forName("org.postgresql.Driver");
            dbConnect = DriverManager.getConnection(url + database, user, password);
            statement = dbConnect.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getResult() { return result; }
    public Statement getStatement() { return statement; }
    public Connection getDbConnect() { return dbConnect; }

    public void dbClose(ResultSet r, Statement s, Connection c) {
        try { if (r != null) r.close(); } catch (Exception ignored) {}
        try { if (s != null) s.close(); } catch (Exception ignored) {}
        try { if (c != null) c.close(); } catch (Exception ignored) {}
    }
}
