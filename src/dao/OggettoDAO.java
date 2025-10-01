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
    public void insertOggetto(OggettoDTO oggetto){
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
        } catch (SQLException e){
            throw new RuntimeException("Errore inserimento oggetto: " + oggetto.getIdOggetto(), e);
        }
    }

    //Ricerca per nome esatto
    public List<OggettoDTO> getOggettiByNome(String nome){
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
        } catch (SQLException e){
            throw new RuntimeException("Errore nella ricerca per nome: " + nome, e);
        }
        return risultato;
    }

    //Ricerca per ID Oggetto
    public OggettoDTO getOggettiById(String id){
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
        } catch (SQLException e){
            throw new RuntimeException("Errore nella ricerca per ID: " + id, e);
        }
        return risultato;
    }

    //Ricerca ordinata per peso crescente
    public List<OggettoDTO> orderOggettiByPeso(){
        String sql = """
                SELECT *
                FROM Oggetto
                ORDER BY Peso_Kg ASC
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
        } catch (SQLException e){
            throw new RuntimeException("Errore nell'ordinamento per peso.");
        }
        return risultato;
    }

    //Ricerca ordinata per numero di proprietari
    public List<OggettoDTO> orderOggettiByNumPropr(){
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
        } catch (SQLException e){
            throw new RuntimeException("Errore nell'ordinamento per numero di proprietari.");
        }
        return risultato;
    }

    //Controllo necessario per l'utilizzo di un oggetto
    public void validazioneOggetto(OggettoDTO o) {
        if (o == null) {
            throw new IllegalArgumentException("Oggetto null");
        }
        if (o.getIdOggetto() == null) {
            throw new IllegalArgumentException("ID oggetto obbligatorio");
        }
        if (o.getNomeOggetto() == null) {
            throw new IllegalArgumentException("Nome oggetto obbligatorio");
        }
        if (o.getProprietario() == null) {
            throw new IllegalArgumentException("Proprietario (FK_Utente) obbligatorio");
        }
        if (o.getCondizione() == null) {
            throw new IllegalArgumentException("Condizione obbligatoria");
        }
        if (o.getDimensione() == null) {
            throw new IllegalArgumentException("Dimensione obbligatoria");
        }
        if (o.getNumProprietari() < 0) {
            throw new IllegalArgumentException("numProprietari negativo");
        }
        if (o.getPeso() < 0) {
            throw new IllegalArgumentException("Peso negativo");
        }
    }

    
}