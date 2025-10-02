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
        try {
            // Validazione campi obbligatori/logica tipo
            annuncio.setDataPubblicazione(LocalDate.now());
            validaInserimento(annuncio);

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
                } else {
                    ps.setNull(10, Types.VARCHAR);
                }
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante inserimento annuncio", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante inserimento annuncio", e);
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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca annunci per tipo", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante ricerca annunci per tipo", e);
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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca annunci per categoria", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante ricerca annunci per categoria", e);
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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca annunci per titolo", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante ricerca annunci per titolo", e);
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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca annunci per prezzo", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante ricerca annunci per prezzo", e);
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
                    risultati.add(new AnnuncioDTO(ID_Annuncio, titolo, descrizione, stato, categoria, dataPub, creatore, ID_Oggetto, tipo, prezzo));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante ricerca annunci per utente", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante ricerca annunci per utente", e);
        }
        return risultati;
    }


    public boolean updateAnnuncio(AnnuncioDTO annuncio) {
        try {
            // Validazione parametri base
            validaUpdate(annuncio);

            // Recupera e valida tipo annuncio esistente
            TipoAnnuncioDTO tipoEsistente = recuperaTipoEsistente(annuncio.getIdAnnuncio());
            if (tipoEsistente == null)
                return false; // annuncio non trovato

            BigDecimal prezzo = annuncio.getPrezzoVendita();
            if (tipoEsistente != TipoAnnuncioDTO.VENDITA) {
                prezzo = null;
            }

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
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante aggiornamento annuncio", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante aggiornamento annuncio", e);
        }
    }


    public boolean deleteAnnuncio(AnnuncioDTO annuncio) {
        try {
            if (annuncio == null) throw new IllegalArgumentException("Annuncio null");
            if (annuncio.getIdAnnuncio() == null || annuncio.getIdAnnuncio().trim().isEmpty())
                throw new IllegalArgumentException("ID annuncio mancante");
            String sql = "DELETE FROM Annuncio WHERE ID_Annuncio = ?";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, annuncio.getIdAnnuncio());
                int deleted = ps.executeUpdate();
                return deleted > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante cancellazione annuncio", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante cancellazione annuncio", e);
        }
    }



    // Validazione campi base per update (senza logica dipendente dal tipo)
    
    private void validaUpdate(AnnuncioDTO annuncio) {
        if (annuncio == null) throw new IllegalArgumentException("Annuncio null");
        if (isBlank(annuncio.getTitolo())) throw new IllegalArgumentException("Errore su Titolo");
        if (annuncio.getCategoria() == null) throw new IllegalArgumentException("Errore su Categoria");
        if (annuncio.getStato() == null) throw new IllegalArgumentException("Errore su Stato");
        if (isBlank(annuncio.getIdOggetto())) throw new IllegalArgumentException("Errore su FK_Oggetto");
    }

    private boolean isBlank(String s){
        return s == null || s.trim().isEmpty();
    }



    // Recupera il tipo esistente dell'annuncio, null se non trovato
    private TipoAnnuncioDTO recuperaTipoEsistente(String idAnnuncio){
        String sql = "SELECT Tipo FROM Annuncio WHERE ID_Annuncio = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Errore DB durante recupero tipo", ex);
        } catch (Exception e) {
            throw new RuntimeException("Errore generico durante recupero tipo", e);
        }
    }

    
    private void validaInserimento(AnnuncioDTO annuncio) {
        if (annuncio == null) throw new IllegalArgumentException("Annuncio null");
        if (isBlank(annuncio.getTitolo())) throw new IllegalArgumentException("Errore su Titolo");
        if (annuncio.getCategoria() == null) throw new IllegalArgumentException("Errore su Categoria");
        if (annuncio.getTipoAnnuncio() == null) throw new IllegalArgumentException("Errore su Tipo");
        if (isBlank(annuncio.getIdOggetto())) throw new IllegalArgumentException("Errore su ID_Oggetto");
        if (isBlank(annuncio.getCreatore())) throw new IllegalArgumentException("Errore su creatore");
        if (annuncio.getDataPubblicazione() == null) throw new IllegalArgumentException("Errore su DataPubblicazione");
    }

}
