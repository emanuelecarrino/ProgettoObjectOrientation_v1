package dao;

import dto.OffertaDTO;
import dto.StatoOffertaDTO;
import dto.TipoOffertaDTO;
import dto.DB_Connection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OffertaDAO {

    private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }

    public void insertOfferta(OffertaDTO offerta) throws SQLException {

        String sql = """ 
            INSERT INTO Offerta 
            (ID_Offerta, PrezzoOfferta, Commento, DataOfferta, Stato, Tipo, FK_Utente, FK_Annuncio, FK_OggettoOfferto)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, offerta.getIdOfferta());
            ps.setFloat(2, offerta.getPrezzoOfferta());
            ps.setString(3, offerta.getCommento());
            ps.setDate(4, Date.valueOf(offerta.getDataOfferta()));
            ps.setString(5, offerta.getStato().name());
            ps.setString(6, offerta.getTipo().name());
            ps.setString(7, offerta.getOfferente());
            ps.setString(8, offerta.getIdAnnuncio());
            if (offerta.getIdOggettoOfferto() != null) 
                ps.setString(9, offerta.getIdOggettoOfferto()); 
            else 
                ps.setNull(9, java.sql.Types.VARCHAR);
            ps.executeUpdate();
        }
    }



    public OffertaDTO getOffertaById(String ID_Offerta) throws SQLException {

        String sql = """
            SELECT *
            FROM Offerta
            WHERE ID_Offerta = ?
            """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ID_Offerta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                float prezzo = rs.getFloat("PrezzoOfferta");
                String commento = rs.getString("Commento");
                LocalDate data = rs.getDate("DataOfferta").toLocalDate();
                StatoOffertaDTO stato = StatoOffertaDTO.valueOf(rs.getString("Stato"));
                TipoOffertaDTO tipo = TipoOffertaDTO.valueOf(rs.getString("Tipo"));
                String offerente = rs.getString("FK_Utente");
                String ID_Annuncio = rs.getString("FK_Annuncio");
                String ID_OggettoOfferto = rs.getString("FK_OggettoOfferto");
                return new OffertaDTO(ID_Offerta, prezzo, commento, data, stato, offerente, tipo, ID_Annuncio, ID_OggettoOfferto);
            }
        }
    }



    public List<OffertaDTO> getOfferteByAnnuncio(String ID_Annuncio) throws SQLException {

        String sql = """
            SELECT *
            FROM Offerta
            WHERE FK_Annuncio = ?
            ORDER BY DataOfferta ASC
            """;

        ArrayList<OffertaDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ID_Annuncio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Offerta = rs.getString("ID_Offerta");
                    float prezzo = rs.getFloat("PrezzoOfferta");
                    String commento = rs.getString("Commento");
                    LocalDate data = rs.getDate("DataOfferta").toLocalDate();
                    StatoOffertaDTO stato = StatoOffertaDTO.valueOf(rs.getString("Stato"));
                    TipoOffertaDTO tipo = TipoOffertaDTO.valueOf(rs.getString("Tipo"));
                    String offerente = rs.getString("FK_Utente");
                    String ID_OggettoOfferto = rs.getString("FK_OggettoOfferto");
                    risultati.add(new OffertaDTO(ID_Offerta, prezzo, commento, data, stato, offerente, tipo, ID_Annuncio, ID_OggettoOfferto));
                }
            }
        }
        return risultati;
    }



    public boolean updateStatoOfferta(String ID_Offerta, StatoOffertaDTO statoCorrente, StatoOffertaDTO statoNuovo) throws SQLException {
        
        String sql = """
            UPDATE Offerta
            SET Stato = ?
            WHERE ID_Offerta = ? AND Stato = ?
            """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statoNuovo.name());
            ps.setString(2, ID_Offerta);
            ps.setString(3, statoCorrente.name());
            return ps.executeUpdate() > 0;
        }
    }



    // Aggiorna prezzo, commento e oggetto offerto SOLO se stato è ancora ATTESA
    
    public boolean updateOfferta(OffertaDTO offerta) throws SQLException {
        
        String sql = """
            UPDATE Offerta
            SET PrezzoOfferta = ?, Commento = ?, FK_OggettoOfferto = ?
            WHERE ID_Offerta = ? AND Stato = 'ATTESA'
            """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setFloat(1, offerta.getPrezzoOfferta());
            ps.setString(2, offerta.getCommento());
            if (offerta.getIdOggettoOfferto() != null) {
                ps.setString(3, offerta.getIdOggettoOfferto());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            ps.setString(4, offerta.getIdOfferta());
            return ps.executeUpdate() > 0;
        }
    }




    public boolean deleteOffertaById(String ID_Offerta) throws SQLException {

        String sql = """
            DELETE FROM Offerta
            WHERE ID_Offerta = ?
            """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ID_Offerta);
            return ps.executeUpdate() > 0;
        }
    }


  

}
