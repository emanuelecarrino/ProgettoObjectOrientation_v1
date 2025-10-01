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
    

    public void insertUtente(UtenteDTO utente){
        if (utente == null) throw new IllegalArgumentException("Utente null");
        if (utente.getMatricola() == null || utente.getMatricola().trim().isEmpty()) throw new IllegalArgumentException("Matricola obbligatoria");
        if (utente.getNome() == null || utente.getNome().trim().isEmpty()) throw new IllegalArgumentException("Nome obbligatorio");
        if (utente.getCognome() == null || utente.getCognome().trim().isEmpty()) throw new IllegalArgumentException("Cognome obbligatorio");
        if (utente.getEmail() == null || utente.getEmail().trim().isEmpty()) throw new IllegalArgumentException("Email obbligatoria");
        if (utente.getUsername() == null || utente.getUsername().trim().isEmpty()) throw new IllegalArgumentException("Username obbligatorio");
        if (utente.getPassword() == null || utente.getPassword().trim().isEmpty()) throw new IllegalArgumentException("Password obbligatoria");
        if (utente.getDataNascita() == null) throw new IllegalArgumentException("DataNascita obbligatoria");

        // Controlli di unicità 
        String checkMatricola = "SELECT 1 FROM Utente WHERE Matricola = ?";
        String checkEmail = "SELECT 1 FROM Utente WHERE Email = ?";
        String checkUsername = "SELECT 1 FROM Utente WHERE Username = ?";

        try (Connection con = getConnection();
             PreparedStatement psMat = con.prepareStatement(checkMatricola);
             PreparedStatement psEmail = con.prepareStatement(checkEmail);
             PreparedStatement psUser = con.prepareStatement(checkUsername)) {

            // Matricola
            psMat.setString(1, utente.getMatricola());
            try (ResultSet rs = psMat.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Matricola già in uso");
                }
            }

            // Email
            psEmail.setString(1, utente.getEmail());
            try (ResultSet rs = psEmail.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Email già in uso");
                }
            }

            // Username
            psUser.setString(1, utente.getUsername());
            try (ResultSet rs = psUser.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Username già in uso");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore controlli unicità per nuovo utente " + utente.getMatricola(), e);
        }

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
        } catch (SQLException e) {
            throw new RuntimeException("Errore inserimento utente " + utente.getMatricola(), e);
        }
    }




    public UtenteDTO getUtenteByUsername(String username){
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca utente per username " + username, e);
        }
    }



    
    public UtenteDTO getUtenteByMatricola (String matricola){
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca utente per matricola " + matricola, e);
        }
    }


    


    public UtenteDTO getUtenteByEmail(String email){
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca utente per email " + email, e);
        }
    }




    public void updateUtente(UtenteDTO utente){
        if (utente == null) throw new IllegalArgumentException("Utente null");
        String matricola = utente.getMatricola();
        if (matricola == null || matricola.trim().isEmpty())
            throw new IllegalArgumentException("Matricola obbligatoria");
            
        if (utente.getNome() == null || utente.getNome().trim().isEmpty())
            throw new IllegalArgumentException("Nome obbligatorio");
        if (utente.getCognome() == null || utente.getCognome().trim().isEmpty())
            throw new IllegalArgumentException("Cognome obbligatorio");
        if (utente.getEmail() == null || utente.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email obbligatoria");
        if (utente.getUsername() == null || utente.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Username obbligatorio");
        if (utente.getPassword() == null || utente.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Password obbligatoria");
        if (utente.getDataNascita() == null)
            throw new IllegalArgumentException("Data nascita obbligatoria");

        // Controllo esistenza utente originale
        String sql = "SELECT Matricola FROM Utente WHERE Matricola = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return; 
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore verifica esistenza utente " + matricola, e);
        }


        // Controlli di unicità per Email e Username
        
        String checkEmail = "SELECT 1 FROM Utente WHERE Email = ? AND Matricola <> ?";
        String checkUsername = "SELECT 1 FROM Utente WHERE Username = ? AND Matricola <> ?";

        try (Connection con = getConnection();
             PreparedStatement psEmail = con.prepareStatement(checkEmail);
             PreparedStatement psUser = con.prepareStatement(checkUsername)) {

            psEmail.setString(1, utente.getEmail());
            psEmail.setString(2, matricola);
            try (ResultSet rs = psEmail.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Email già in uso");
                }
            }

            psUser.setString(1, utente.getUsername());
            psUser.setString(2, matricola);
            try (ResultSet rs = psUser.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Username già in uso");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore controlli unicità per email/username utente " + matricola, e);
        }


        // Esegui UPDATE 

        sql = """
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
            ps.setString(8, matricola);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento utente " + matricola, e);
        }
    }






    
    public void deleteUtente(UtenteDTO utente){
        
        
        
        //Controlla argomento null
        if (utente == null) throw new IllegalArgumentException("Utente null");

        //Controlla esistenza utente
        String sql = "SELECT Matricola FROM Utente WHERE Matricola = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, utente.getMatricola());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return; 
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore verifica esistenza utente " + utente.getMatricola(), e);
        }
        
        sql = """
            DELETE *
            FROM Utente
            WHERE Matricola = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            

        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione utente " + utente.getMatricola(), e);
        }

        
        
        


    }



}
