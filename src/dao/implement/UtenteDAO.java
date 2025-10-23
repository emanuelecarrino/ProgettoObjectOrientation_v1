package dao.implement;

import dao.interf.UtenteDAOinterf;
import dto.DB_Connection;
import dto.UtenteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class UtenteDAO implements UtenteDAOinterf {

    private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }

    @Override
    public void insertUtente(UtenteDTO utente) throws SQLException {
        // Assunto: validazione fatta nel Controller
        String sql = """
            INSERT INTO Utente
            (Nome, Cognome, Email, Matricola, Username, Password, DataNascita, Genere)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getMatricola());
            ps.setString(5, utente.getUsername());
            ps.setString(6, utente.getPassword());
            ps.setDate(7, java.sql.Date.valueOf(utente.getDataNascita()));
            ps.setString(8, utente.getGenere());
            ps.executeUpdate();
        }
    }






    @Override
    public UtenteDTO getUtenteByUsername(String username) throws SQLException {
        String sql = """
            SELECT *
            FROM Utente
            WHERE Username = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String email = rs.getString("Email");
                String matricola = rs.getString("Matricola");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        }
    }



    
    @Override
    public UtenteDTO getUtenteByMatricola (String matricola) throws SQLException {
        String sql = """
            SELECT *
            FROM Utente
            WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String email = rs.getString("Email");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        }
    }


    


    @Override
    public UtenteDTO getUtenteByEmail(String email) throws SQLException {
        String sql = """
                SELECT *
                FROM Utente
                WHERE Email = ?
                """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String username = rs.getString("Username");
                String matricola = rs.getString("Matricola");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        }
    }




    @Override
    public boolean updateUtente(UtenteDTO utente) throws SQLException {
        String sql = """
            UPDATE Utente
            SET Nome = ?, Cognome = ?, Email = ?, Username = ?, Password = ?, DataNascita = ?, Genere = ?
            WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getUsername());
            ps.setString(5, utente.getPassword());
            ps.setDate(6, java.sql.Date.valueOf(utente.getDataNascita()));
            ps.setString(7, utente.getGenere());
            ps.setString(8, utente.getMatricola());
            return ps.executeUpdate() > 0;
        }
    }




    
    @Override
    public boolean deleteUtenteByMatricola(String matricola) throws SQLException {
        String sql = """
            DELETE FROM Utente WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricola);
            return ps.executeUpdate() > 0;
        }
    }




    // Validazione per l'inserimento di un nuovo utente

    // Rimosse validazioni: demandate al Controller


}
