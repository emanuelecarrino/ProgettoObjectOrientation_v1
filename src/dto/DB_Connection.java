package dto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;



public class DB_Connection {
    private static Connection c;
    private static boolean done;

    public static synchronized Connection getConnection() throws SQLException {


        String host = val("DB_HOST","localhost");
        String port = val("DB_PORT","5432");
        String db   = val("DB_NAME","uninaswap");
        String usr  = val("DB_USER","postgres");
        String pwd  = val("DB_PASSWORD","password");
        String schema = val("DB_SCHEMA","uninaswap");
        Path sqlDir = Paths.get("src","sql");

        String[] scripts = {
            "1_uninaswap_create_schema.sql",
            "2_uninaswap_create_tables.sql",
            "3_uninaswap_constraints.sql",
            "4_uninaswap_trigger_functions.sql",
            "5_uninaswap_trigger.sql",
            "6_uninaswap_functions_procedures.sql",
            "7_uninaswap_popolamento.sql"};

        // Crea/ottiene connessione
        if (c == null || c.isClosed()) {
            try { c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+db, usr, pwd); }
            catch (SQLException e) {
                if ("3D000".equals(e.getSQLState())) { // DB mancante
                    try (Connection root = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/postgres", usr, pwd);
                         Statement st = root.createStatement()) {
                        st.executeUpdate("CREATE DATABASE \""+db+"\"");
                    }
                    c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+db, usr, pwd);
                } else throw e;
            }
        }

        // Prima inizializzazione: crea schema e (se necessario) esegue script

        if (!done) {
            try (Statement st = c.createStatement()) {
                st.executeUpdate("CREATE SCHEMA IF NOT EXISTS \""+schema+"\"");
                st.execute("SET search_path TO "+schema+", public");
            }
            // controlla tabella Utente
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM information_schema.tables WHERE table_schema=? AND table_name=?")) {
                ps.setString(1, schema.toLowerCase());
                ps.setString(2, "utente");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        if (Files.isDirectory(sqlDir)) {
                            for (String s : scripts) {
                                Path p = sqlDir.resolve(s);
                                if (Files.exists(p)) {
                                    try {
                                        String content = Files.readString(p, StandardCharsets.UTF_8);
                                        String[] parts = content.split(";\\n?");
                                        for (String raw : parts) {
                                            String stmt = raw.trim();
                                            if (stmt.isEmpty() || stmt.startsWith("--")) continue;
                                            try (Statement st2 = c.createStatement()) { st2.execute(stmt); }
                                        }
                                    } catch (IOException ex) {
                                        throw new SQLException("Errore script "+p+": "+ex.getMessage(), ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            done = true;
        }
        return c;
    }

    private static String val(String k, String def) {
        String v = System.getenv(k);
        if (v == null || v.isEmpty()) v = System.getProperty(k, def);
        return v;
    }
}