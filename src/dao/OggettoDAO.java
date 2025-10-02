package dao;

import dto.OggettoDTO;
import dto.UtenteDTO;
import dto.AnnuncioDTO;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;

import dto.DB_Connection;
import java.sql.SQLException;

public class OggettoDAO {
    
    private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }

    //Inserimento oggetto
    public void insertOggetto(OggettoDTO oggetto) {
        try {
            validazioneOggetto(oggetto);
            String sql = """
                INSERT INTO Oggetto(ID_Oggetto, Nome, numProprietari, Condizioni, Dimensione, Peso_Kg, FK_Utente)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
                ps.setString(1, oggetto.getIdOggetto());
                ps.setString(2, oggetto.getNomeOggetto());
                ps.setInt(3, oggetto.getNumProprietari());
                ps.setString(4, oggetto.getCondizione());
                ps.setString(5, oggetto.getDimensione());
                ps.setFloat(6, oggetto.getPeso());
                ps.setString(7, oggetto.getProprietario());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante inserimento oggetto", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante inserimento oggetto", e);
        }
    }

    //Ricerca per proprietario
    public List<OggettoDTO> getOggettiByPropr(String proprietario) {
        String sql = """
                SELECT *
                FROM Oggetto
                WHERE FK_Utente = ?
                """;
        ArrayList<OggettoDTO> risultato = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, proprietario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = rs.getString("ID_Oggetto");
                String nome = rs.getString("Nome");
                int numProp = rs.getInt("numProprietari");
                String condizioni = rs.getString("Condizioni");
                String dimensione = rs.getString("Dimensione");
                Float peso = rs.getFloat("Peso_Kg");
                risultato.add(new OggettoDTO(id, nome, numProp, condizioni, dimensione, peso, proprietario));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca per proprietario", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante ricerca per proprietario", e);
        }
        return risultato;
    }

    //Ricerca per nome esatto
    public List<OggettoDTO> getOggettiByNome(String nome) {
        String sql = """
                SELECT *
                FROM Oggetto
                WHERE Nome = ?
                """;
        ArrayList<OggettoDTO> risultato = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = rs.getString("ID_Oggetto");
                int numProp = rs.getInt("numProprietari");
                String condizioni = rs.getString("Condizioni");
                String dimensione = rs.getString("Dimensione");
                Float peso = rs.getFloat("Peso_Kg");
                String proprietario = rs.getString("FK_Utente");
                risultato.add(new OggettoDTO(id, nome, numProp, condizioni, dimensione, peso, proprietario));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca per nome", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante ricerca per nome", e);
        }
        return risultato;
    }

    //Ricerca per ID Oggetto
    public OggettoDTO getOggettiById(String id) {
        String sql = """
                SELECT *
                FROM Oggetto
                WHERE ID_Oggetto = ?
                """;
        OggettoDTO risultato = null;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                String nomeOgg = rs.getString("Nome");
                int numProp = rs.getInt("numProprietari");
                String condizioni = rs.getString("Condizioni");
                String dimensione = rs.getString("Dimensione");
                Float peso = rs.getFloat("Peso_Kg");
                String proprietario = rs.getString("FK_Utente"); 
                risultato = new OggettoDTO(id, nomeOgg, numProp, condizioni, dimensione, peso, proprietario);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca per ID", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante ricerca per ID", e);
        }
        return risultato;
    }

    //Ricerca ordinata per peso -crescente/decrescente-
    public List<OggettoDTO> orderOggettiByPeso(String ordine) {
        
        String sql = """
                SELECT *
                FROM Oggetto
                ORDER BY Peso_Kg 
                """ + ordine;
        
        ArrayList<OggettoDTO> risultato = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = rs.getString("ID_Oggetto");
                String nome = rs.getString("Nome");
                int numProp = rs.getInt("numProprietari");
                String condizioni = rs.getString("Condizioni");
                String dimensione = rs.getString("Dimensione");
                Float peso = rs.getFloat("Peso_Kg");
                String proprietario = rs.getString("FK_Utente");
                risultato.add(new OggettoDTO(id, nome, numProp, condizioni, dimensione, peso, proprietario));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ordinamento per peso", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante ordinamento per peso", e);
        }
        return risultato;
    }

    //Ricerca ordinata per numero di proprietari
    public List<OggettoDTO> orderOggettiByNumPropr() {
        String sql = """
                SELECT *
                FROM Oggetto
                ORDER BY numProprietari ASC
                """;
        ArrayList<OggettoDTO> risultato = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String id = rs.getString("ID_Oggetto");
                String nome = rs.getString("Nome");
                int numProp = rs.getInt("numProprietari");
                String condizioni = rs.getString("Condizioni");
                String dimensione = rs.getString("Dimensione");
                Float peso = rs.getFloat("Peso_Kg");
                String proprietario = rs.getString("FK_Utente");
                risultato.add(new OggettoDTO(id, nome, numProp, condizioni, dimensione, peso, proprietario));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ordinamento per numero di proprietari", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante ordinamento per numero di proprietari", e);
        }
        return risultato;
    }



    public void updateOggetto(OggettoDTO oggetto){

    }





    //Controllo necessario per l'utilizzo di un oggetto
    public void validazioneOggetto(OggettoDTO o) {
        if (o == null) throw new IllegalArgumentException("Oggetto null");
        if (o.getIdOggetto() == null || o.getIdOggetto().trim().isEmpty()) throw new IllegalArgumentException("Errore su ID_Oggetto");
        if (o.getNomeOggetto() == null || o.getNomeOggetto().trim().isEmpty()) throw new IllegalArgumentException("Errore su Nome");
        if (o.getProprietario() == null || o.getProprietario().trim().isEmpty()) throw new IllegalArgumentException("Errore su creatore");
        if (o.getCondizione() == null || o.getCondizione().trim().isEmpty()) throw new IllegalArgumentException("Errore su Condizioni");
    }

    
}