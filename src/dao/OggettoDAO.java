package dao;

import dto.OggettoDTO;
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
    public void insertOggetto(OggettoDTO oggetto) throws SQLException {

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
    }

    //Ricerca per proprietario
    public List<OggettoDTO> getOggettiByPropr(String proprietario) throws SQLException {
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
        }
        return risultato;
    }

    //Ricerca per nome esatto
    public List<OggettoDTO> getOggettiByNome(String nome) throws SQLException {
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
        }
        return risultato;
    }

    //Ricerca per ID Oggetto
    public OggettoDTO getOggettiById(String id) throws SQLException {
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
        }
        return risultato;
    }

    //Ricerca ordinata per peso -crescente/decrescente-
    public List<OggettoDTO> orderOggettiByPeso(String ordine) throws SQLException{
        
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
        }
        return risultato;
    }



    //Ricerca ordinata per numero di proprietari
    public List<OggettoDTO> orderOggettiByNumPropr() throws SQLException {
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
        }
        return risultato;
    }



    public boolean updateOggetto(OggettoDTO oggetto) throws SQLException {

        String sql = """
            UPDATE Oggetto
            SET Nome = ?, numProprietari = ?, Condizioni = ?, Dimensione = ?, Peso_Kg = ?
            WHERE ID_Oggetto = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, oggetto.getNomeOggetto());
            ps.setInt(2, oggetto.getNumProprietari());
            ps.setString(3, oggetto.getCondizione());
            ps.setString(4, oggetto.getDimensione());
            if (oggetto.getPeso() != null) ps.setFloat(5, oggetto.getPeso()); else ps.setNull(5, java.sql.Types.FLOAT);
            ps.setString(6, oggetto.getIdOggetto());
            return ps.executeUpdate() > 0;
        }
    }



    public boolean deleteOggettoById(String idOggetto) throws SQLException {
        String sql = "DELETE FROM Oggetto WHERE ID_Oggetto = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idOggetto);
            return ps.executeUpdate() > 0;
        }
    }


}