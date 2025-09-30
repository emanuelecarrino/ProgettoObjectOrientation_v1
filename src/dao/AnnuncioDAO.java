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

    public void creaAnnuncio(AnnuncioDTO ann) {
        validaInserimento(ann); // <-- validazione interna

        String sql = """
            INSERT INTO Annuncio
            (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato,
             Tipo, PrezzoVendita, FK_Oggetto, FK_Utente)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ann.getIdAnnuncio());
            ps.setString(2, ann.getTitolo());
            ps.setString(3, ann.getDescrizione());
            ps.setDate(4, java.sql.Date.valueOf(ann.getDataPubblicazione()));
            ps.setString(5, ann.getCategoria().name());
            ps.setString(6, ann.getStato().name());
            ps.setString(7, ann.getTipoAnnuncio().name());


            if (ann.getTipoAnnuncio() == TipoAnnuncioDTO.VENDITA) {
                ps.setBigDecimal(8, ann.getPrezzoVendita());
            } else {
                ps.setNull(8, java.sql.Types.DECIMAL);
            }

            ps.setString(9, ann.getOggetto().getIdOggetto());
            String matricolaCreatore = null;
            if (ann.getCreatore() != null) {
                matricolaCreatore = ann.getCreatore().getMatricola();
            }
            ps.setString(10, matricolaCreatore);

            ps.executeUpdate();

           
        } catch (SQLException e) {
            throw new RuntimeException("Errore inserimento annuncio " + ann.getIdAnnuncio(), e);
        }

    }




    public List<AnnuncioDTO> cercaPerTipo(TipoAnnuncioDTO tipo){
        if (tipo == null) throw new IllegalArgumentException("Tipo null");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE Tipo = ?
            """;

        List<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipoRow = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String idOggetto = rs.getString("FK_Oggetto");
                    String matricola = rs.getString("FK_Utente");
                    OggettoDTO oggetto = new OggettoDTO(idOggetto, null, 0, null, null, null, null);
                    UtenteDTO creatore = new UtenteDTO(null, null, null, matricola, null, null, null, null);
                    risultati.add(new AnnuncioDTO(id, titolo, descrizione, stato, categoria, dataPub, creatore, oggetto, tipoRow, prezzo));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per tipo " + tipo, e);
        }
        return risultati;
    }



    public List<AnnuncioDTO> cercaPerTitolo(String ricerca) {
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

        List<AnnuncioDTO> risultati = new ArrayList<>();
        String pattern = "%" + trimmed.toLowerCase() + "%";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzo = rs.getBigDecimal("PrezzoVendita");
                    String idOggetto = rs.getString("FK_Oggetto");
                    String matricola = rs.getString("FK_Utente");
                    OggettoDTO oggetto = new OggettoDTO(idOggetto, null, 0, null, null, null, null);
                    UtenteDTO creatore = new UtenteDTO(null, null, null, matricola, null, null, null, null);
                    risultati.add(new AnnuncioDTO(id, titolo, descrizione, stato, categoria, dataPub, creatore, oggetto, tipo, prezzo));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per titolo contenente '" + ricerca + "'", e);
        }
        return risultati;
    }



    public List<AnnuncioDTO> cercaPerPrezzo(BigDecimal prezzoMin, BigDecimal prezzoMax) {
        if (prezzoMin == null && prezzoMax == null) throw new IllegalArgumentException("Prezzo null");
        if (prezzoMin.signum() < 0 && prezzoMax.signum() < 0) throw new IllegalArgumentException("Prezzo negativo");

        String sql = """
            SELECT ID_Annuncio, Titolo, Descrizione, DataPubblicazione,
                   Categoria, Stato, Tipo, PrezzoVendita,
                   FK_Oggetto, FK_Utente
            FROM Annuncio
            WHERE PrezzoVendita > ? AND PrezzoVendita < ?
            """;

        List<AnnuncioDTO> risultati = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, prezzoMin);
            ps.setBigDecimal(2, prezzoMax);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID_Annuncio");
                    String titolo = rs.getString("Titolo");
                    String descrizione = rs.getString("Descrizione");
                    LocalDate dataPub = rs.getDate("DataPubblicazione").toLocalDate();
                    CategoriaAnnuncioDTO categoria = CategoriaAnnuncioDTO.valueOf(rs.getString("Categoria"));
                    StatoAnnuncioDTO stato = StatoAnnuncioDTO.valueOf(rs.getString("Stato"));
                    TipoAnnuncioDTO tipo = TipoAnnuncioDTO.valueOf(rs.getString("Tipo"));
                    BigDecimal prezzoDb = rs.getBigDecimal("PrezzoVendita");
                    String idOggetto = rs.getString("FK_Oggetto");
                    String matricola = rs.getString("FK_Utente");
                    OggettoDTO oggetto = new OggettoDTO(idOggetto, null, 0, null, null, null, null);
                    UtenteDTO creatore = new UtenteDTO(null, null, null, matricola, null, null, null, null);
                    risultati.add(new AnnuncioDTO(id, titolo, descrizione, stato, categoria, dataPub, creatore, oggetto, tipo, prezzoDb));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca annunci per prezzo tra " + prezzoMin + " e " + prezzoMax  , e);
        }
        return risultati;
    }




    private void validaInserimento(AnnuncioDTO ann) {
        
        if (ann == null) throw new IllegalArgumentException("Annuncio Null");
        String titolo = ann.getTitolo();
        if (titolo == null || titolo.trim().isEmpty()) throw new IllegalArgumentException("Titolo obbligatorio");
        if (ann.getCategoria() == null) throw new IllegalArgumentException("Categoria obbligatoria");
        if (ann.getTipoAnnuncio() == null) throw new IllegalArgumentException("Tipo obbligatorio");
        if (ann.getOggetto().getIdOggetto() == null) throw new IllegalArgumentException("Oggetto mancante");
        if (ann.getCreatore() == null || ann.getCreatore().getMatricola() == null)
            throw new IllegalArgumentException("Utente mancante");
        if (ann.getDataPubblicazione() == null) throw new IllegalArgumentException("Data pubblicazione mancante");

        switch (ann.getTipoAnnuncio()) {
            case VENDITA -> {
                if (ann.getPrezzoVendita() == null || ann.getPrezzoVendita().signum() <= 0)
                    throw new IllegalArgumentException("Prezzo non valido");
            }
            case SCAMBIO -> {
                if (ann.getPrezzoVendita() != null)
                    throw new IllegalArgumentException("Il prezzo deve essere null per uno scambio");
            }
            case REGALO -> {
                if (ann.getPrezzoVendita() != null)
                    throw new IllegalArgumentException("Il prezzo deve essere null per un regalo");
            }
        }
    }


    // ... findById(), findByTipo(), listAttivi(), aggiornaStato(), elimina() come prima




}
