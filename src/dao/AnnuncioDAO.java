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


    public void insertAnnuncio(AnnuncioDTO annuncio) {
        validaInserimento(annuncio); // <-- validazione interna

        String sql = """
            INSERT INTO Annuncio
            (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato,
             Tipo, PrezzoVendita, FK_Oggetto, FK_Utente)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
            if (annuncio.getCreatore() != null) {
                ps.setString(10, annuncio.getCreatore());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore inserimento annuncio " + annuncio.getIdAnnuncio(), e);
        }
    }


    public List<AnnuncioDTO> getAnnunciByTipo(TipoAnnuncioDTO tipo){
        if (tipo == null) throw new IllegalArgumentException("Tipo null");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE Tipo = ?
            """;

        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per tipo " + tipo, e);
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByCategoria(CategoriaAnnuncioDTO categoria){
        if (categoria == null) throw new IllegalArgumentException("Tipo null");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE Categoria = ?
            """;

        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per categoria " + categoria, e);
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByTitolo(String ricerca) {
        if (ricerca == null) throw new IllegalArgumentException("Ricerca null");
        String trimmed = ricerca.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Ricerca vuota");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE LOWER(Titolo) LIKE ?
            """;

        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        String pattern = "%" + trimmed.toLowerCase() + "%";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per titolo contenente '" + ricerca + "'", e);
        }
        return risultati;
    }



    public List<AnnuncioDTO> getAnnunciByPrezzoMax(BigDecimal prezzoMax) {
        if (prezzoMax == null) throw new IllegalArgumentException("Prezzo massimo null");
        if (prezzoMax.signum() < 0) throw new IllegalArgumentException("Prezzo massimo negativo");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE PrezzoVendita <= ?
            """;

        ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per prezzo <= " + prezzoMax, e);
        }
        return risultati;
    }


    public List<AnnuncioDTO> getAnnunciByCreatore(String creatore){
        if (creatore == null || creatore.trim().isEmpty()) 
            throw new IllegalArgumentException("Matricola utente mancante");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE FK_Utente = ?
            """;

    ArrayList<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per creatore " + creatore, e);
        }
        return risultati;
    }

    //boolean?
    public boolean updateAnnuncio(AnnuncioDTO annuncio) {
        if (annuncio == null) throw new IllegalArgumentException("Annuncio aggiornato null");
        if (annuncio.getIdAnnuncio() == null || annuncio.getIdAnnuncio().trim().isEmpty())
            throw new IllegalArgumentException("ID annuncio mancante");


        String sql = "SELECT Tipo FROM Annuncio WHERE ID_Annuncio = ?";
        TipoAnnuncioDTO tipoEsistente;
        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, annuncio.getIdAnnuncio());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                tipoEsistente = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura tipo annuncio " + annuncio.getIdAnnuncio(), e);
        }

        String titolo = annuncio.getTitolo();
        if (titolo == null || titolo.trim().isEmpty())
            throw new IllegalArgumentException("Titolo obbligatorio");
        if (annuncio.getCategoria() == null)
            throw new IllegalArgumentException("Categoria obbligatoria");
        if (annuncio.getStato() == null)
            throw new IllegalArgumentException("Stato obbligatorio");
        if (annuncio.getIdOggetto() == null)
            throw new IllegalArgumentException("Oggetto obbligatorio");


        BigDecimal prezzo = annuncio.getPrezzoVendita();
        switch (tipoEsistente) {
            case VENDITA -> {
                if (prezzo == null || prezzo.signum() <= 0)
                    throw new IllegalArgumentException("Prezzo non valido per VENDITA");
            }
            case SCAMBIO, REGALO -> {
                prezzo = null;
            }
        }


        sql = """
            UPDATE Annuncio
            SET Titolo = ?, Descrizione = ?, Categoria = ?, Stato = ?, PrezzoVendita = ?, FK_Oggetto = ?
            WHERE ID_Annuncio = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, annuncio.getDescrizione());
            ps.setString(3, annuncio.getCategoria().name());
            ps.setString(4, annuncio.getStato().name());
            if (prezzo != null) {
                ps.setBigDecimal(5, prezzo);
            } else {
                ps.setNull(5, Types.DECIMAL);
            }
            ps.setString(6, annuncio.getIdOggetto());
            ps.setString(7, annuncio.getIdAnnuncio());


            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento annuncio " + annuncio.getIdAnnuncio(), e);
        }
    }


    public boolean deleteAnnuncio(AnnuncioDTO annuncio) {
        if (annuncio == null) throw new IllegalArgumentException("Annuncio null");
        if (annuncio.getIdAnnuncio() == null || annuncio.getIdAnnuncio().trim().isEmpty())
            throw new IllegalArgumentException("ID annuncio mancante");

        String sql = "DELETE FROM Annuncio WHERE ID_Annuncio = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, annuncio.getIdAnnuncio());
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione annuncio " + annuncio.getIdAnnuncio(), e);
        }
    }

    
    private void validaInserimento(AnnuncioDTO annuncio) {
        
        if (annuncio == null) throw new IllegalArgumentException("Annuncio Null");
        String titolo = annuncio.getTitolo();
        if (titolo == null || titolo.trim().isEmpty()) throw new IllegalArgumentException("Titolo obbligatorio");
        if (annuncio.getCategoria() == null) throw new IllegalArgumentException("Categoria obbligatoria");
        if (annuncio.getTipoAnnuncio() == null) throw new IllegalArgumentException("Tipo obbligatorio");
        if (annuncio.getIdOggetto() == null) throw new IllegalArgumentException("Oggetto mancante");
        if (annuncio.getCreatore() == null || annuncio.getCreatore() == null)
            throw new IllegalArgumentException("Utente mancante");
        if (annuncio.getDataPubblicazione() == null) throw new IllegalArgumentException("Data pubblicazione mancante");

        switch (annuncio.getTipoAnnuncio()) {
            case VENDITA -> {
                if (annuncio.getPrezzoVendita() == null || annuncio.getPrezzoVendita().signum() <= 0)
                    throw new IllegalArgumentException("Prezzo non valido");
            }
            case SCAMBIO -> {
                if (annuncio.getPrezzoVendita() != null)
                    throw new IllegalArgumentException("Il prezzo deve essere null per uno scambio");
            }
            case REGALO -> {
                if (annuncio.getPrezzoVendita() != null)
                    throw new IllegalArgumentException("Il prezzo deve essere null per un regalo");
            }
        }
    }
}
