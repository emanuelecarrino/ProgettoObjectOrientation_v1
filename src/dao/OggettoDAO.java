package dao;

import dto.OggettoDTO;
import dto.UtenteDTO;
import dto.AnnuncioDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;

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

    //Controllo necessario per l'utilizzo di un oggetto
    private void validazioneOggetto(OggettoDTO o) {
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