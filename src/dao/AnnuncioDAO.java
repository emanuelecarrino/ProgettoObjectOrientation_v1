package dao;

import dto.DB_Connection;
import dto.AnnuncioDTO;
import dto.TipoAnnuncioDTO;
import dto.StatoAnnuncioDTO;
import dto.CategoriaAnnuncioDTO;
import dto.UtenteDTO;
import dto.OggettoDTO;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;



public class AnnuncioDAO {

    private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }


    public void insertAnnuncio(AnnuncioDTO annuncio) throws SQLException {

        String sql = """
            INSERT INTO Annuncio
            (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato,
             Tipo, PrezzoVendita, FK_Oggetto, FK_Utente)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, annuncio.getIdAnnuncio());
            ps.setString(2, annuncio.getTitolo());
            ps.setString(3, annuncio.getDescrizione());
            ps.setDate(4, java.sql.Date.valueOf(annuncio.getDataPubblicazione()));
            ps.setString(5, annuncio.getCategoria().name());
            ps.setString(6, annuncio.getStato().name());
            ps.setString(7, annuncio.getTipoAnnuncio().name());
            if (annuncio.getTipoAnnuncio() == TipoAnnuncioDTO.VENDITA) {
                ps.setBigDecimal(8, annuncio.getPrezzoVendita());
            } else {
                ps.setNull(8, java.sql.Types.DECIMAL);
            }
            ps.setString(9, annuncio.getIdOggetto());
            if (annuncio.getCreatore() != null) ps.setString(10, annuncio.getCreatore()); else ps.setNull(10, Types.VARCHAR);
            ps.executeUpdate();
        }
    }


    public List<AnnuncioDTO> getAnnunciByTipo(TipoAnnuncioDTO tipo) throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE Tipo = ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipoRow = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    String creatore = rs.getString("FK_Utente");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipoRow, prezzo));
                }
            }
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByCategoria(CategoriaAnnuncioDTO categoria) throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE Categoria = ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO cat = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    String creatore = rs.getString("FK_Utente");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByTitolo(String ricerca) throws SQLException {
        String trimmed = ricerca; // validazione spostata al Controller
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE LOWER(Titolo) LIKE ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        String pattern = "%" + trimmed.toLowerCase().trim() + "%";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    String creatore = rs.getString("FK_Utente");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByPrezzoMax(BigDecimal prezzoMax) throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE PrezzoVendita <= ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, prezzoMax);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    String creatore = rs.getString("FK_Utente");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        }
        return risultati;
    }




    public List<AnnuncioDTO> getAllAnnunci() throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE StatoAnnuncio = ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "Attivo");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    String creatore = rs.getString("FK_Utente");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, StatoAnnuncioDTO.ATTIVO , categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        }
        return risultati;
    }






    public List<AnnuncioDTO> getAnnunciByCreatore(String creatore) throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE FK_Utente = ?
            """;
        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, creatore);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ID_Annuncio = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String ID_Oggetto = rs.getString("FK_Oggetto");
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        }
        return risultati;
    }


    public boolean updateAnnuncio(AnnuncioDTO annuncio) throws SQLException {
        TipoAnnuncioDTO tipoEsistente = recuperaTipoEsistente(annuncio.getIdAnnuncio());
        if (tipoEsistente == null) return false;
        BigDecimal prezzo = annuncio.getPrezzoVendita();
        if (tipoEsistente != TipoAnnuncioDTO.VENDITA) prezzo = null;
        String sql = """
            UPDATE Annuncio
            SET Titolo = ?, Descrizione = ?, Categoria = ?, Stato = ?, PrezzoVendita = ?
            WHERE ID_Annuncio = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, annuncio.getTitolo());
            ps.setString(2, annuncio.getDescrizione());
            ps.setString(3, annuncio.getCategoria().name());
            ps.setString(4, annuncio.getStato().name());
            if (prezzo != null) ps.setBigDecimal(5, prezzo); else ps.setNull(5, Types.DECIMAL);
            ps.setString(6, annuncio.getIdAnnuncio());
            return ps.executeUpdate() > 0;
        }
    }


    public boolean deleteAnnuncioById(String ID_Annuncio) throws SQLException {
        String sql = "DELETE FROM Annuncio WHERE ID_Annuncio = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ID_Annuncio);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    // Recupero annuncio completo per ID 
    public AnnuncioDTO getAnnuncioById(String idAnnuncio) throws SQLException {
        String sql = """
            SELECT *
            FROM Annuncio
            WHERE ID_Annuncio = ?
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String ID_Annuncio = rs.getString("ID_Annuncio");
                String titolo = rs.getString("Titolo");
                String descrizione = rs.getString("Descrizione");
                LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                String ID_Oggetto = rs.getString("FK_Oggetto");
                String creatore = rs.getString("FK_Utente");
                return new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo);
            }
        }
    }

    // Recupera il tipo esistente dell'annuncio, null se non trovato
    private TipoAnnuncioDTO recuperaTipoEsistente(String idAnnuncio) throws SQLException {
        String sql = "SELECT Tipo FROM Annuncio WHERE ID_Annuncio = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
            }
        }
    }


}
