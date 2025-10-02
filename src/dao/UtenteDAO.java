package dao;

import dto.AnnuncioDTO;
import dto.CategoriaAnnuncioDTO;
import dto.DB_Connection;
import dto.OggettoDTO;
import dto.StatoAnnuncioDTO;
import dto.TipoAnnuncioDTO;
import dto.UtenteDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {
    
            private Connection getConnection() throws SQLException {
                return DB_Connection.getConnection();
        }
    

    public void insertUtente(UtenteDTO utente) {
        try {
            // Validazione campi base (solo presenza campi obbligatori)
            validaInserimento(utente);
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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante inserimento utente.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante inserimento utente.", e);
        }
    }






    public UtenteDTO getUtenteByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username null");
        String sql = """
            SELECT Nome, Cognome, Email, Matricola, Username, Password, DataNascita, Genere
            FROM Utente
            WHERE Username = ?
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String email = rs.getString("Email");
                String matricola = rs.getString("Matricola");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante la ricerca per username.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca per username.", e);
        }
    }



    
    public UtenteDTO getUtenteByMatricola (String matricola) {
        if (matricola == null) throw new IllegalArgumentException("Matricola null");
        String sql = """
            SELECT *
            FROM Utente
            WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String email = rs.getString("Email");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante la ricerca per matricola.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca per matricola.", e);
        }
    }


    


    public UtenteDTO getUtenteByEmail(String email) {
        if(email == null) throw new IllegalArgumentException("Email null");
        String sql = """
                SELECT *
                FROM Utente
                WHERE Email = ?
                """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String nome = rs.getString("Nome");
                String cognome = rs.getString("Cognome");
                String username = rs.getString("Username");
                String matricola = rs.getString("Matricola");
                String password = rs.getString("Password");
                String dataNascita = rs.getString("DataNascita");
                String genere = rs.getString("Genere");
                return new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante la ricerca per email.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca per email.", e);
        }
    }




    public void updateUtente(UtenteDTO utente) {
        if (utente == null) throw new IllegalArgumentException("Utente null");
        String matricola = utente.getMatricola();
        if (matricola == null || matricola.trim().isEmpty())
            throw new IllegalArgumentException("Errore su Matricola");
            
        if (utente.getNome() == null || utente.getNome().trim().isEmpty())
            throw new IllegalArgumentException("Errore su Nome");
        if (utente.getCognome() == null || utente.getCognome().trim().isEmpty())
            throw new IllegalArgumentException("Errore su Cognome");
        if (utente.getEmail() == null || utente.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Errore su Email");
        if (utente.getUsername() == null || utente.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Errore su Username");
        if (utente.getPassword() == null || utente.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Errore su Password");
        if (utente.getDataNascita() == null)
            throw new IllegalArgumentException("Errore su DataNascita");

        // Controllo esistenza utente originale
        String sql = "SELECT Matricola FROM Utente WHERE Matricola = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return; 
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante aggiornamento utente.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante aggiornamento utente.", e);
        }


    // Unicità Email / Username demandata ai vincoli UNIQUE del DB


        // Esegui UPDATE 

        sql = """
            UPDATE Utente
            SET Nome = ?, Cognome = ?, Email = ?, Username = ?, Password = ?, DataNascita = ?, Genere = ?
            WHERE Matricola = ?
            """;

        try (Connection con2 = getConnection(); PreparedStatement ps2 = con2.prepareStatement(sql)) {
            ps2.setString(1, utente.getNome());
            ps2.setString(2, utente.getCognome());
            ps2.setString(3, utente.getEmail());
            ps2.setString(4, utente.getUsername());
            ps2.setString(5, utente.getPassword());
            ps2.setDate(6, java.sql.Date.valueOf(utente.getDataNascita()));
            ps2.setString(7, utente.getGenere());
            ps2.setString(8, matricola);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante agggiornamento utente.", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante aggiornamento utente.", e);
        }
    }




    
    public void deleteUtente(UtenteDTO utente) {
        //Controlla argomento null
        if (utente == null) throw new IllegalArgumentException("Utente null");
        String sql = """
            DELETE 
            FROM Utente
            WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utente.getMatricola());
            int esiste = ps.executeUpdate();
            if (esiste == 0) {
                throw new IllegalArgumentException("Utente inesistente");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante cancellazione utente", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante cancellazione utente", e);
        }
    }




    // Validazione per l'inserimento di un nuovo utente

    private void validaInserimento(UtenteDTO utente) {
        if (utente == null) throw new IllegalArgumentException("Utente null");
    if (isBlank(utente.getMatricola())) throw new IllegalArgumentException("Errore su Matricola");
    if (isBlank(utente.getNome())) throw new IllegalArgumentException("Errore su Nome");
    if (isBlank(utente.getCognome())) throw new IllegalArgumentException("Errore su Cognome");
    if (isBlank(utente.getEmail())) throw new IllegalArgumentException("Errore su Email");
    if (isBlank(utente.getUsername())) throw new IllegalArgumentException("Errore su Username");
    if (isBlank(utente.getPassword())) throw new IllegalArgumentException("Errore su Password");
    if (utente.getDataNascita() == null) throw new IllegalArgumentException("Errore su DataNascita");
}

    private boolean isBlank(String s){
        return s == null || s.trim().isEmpty();
    }


}
