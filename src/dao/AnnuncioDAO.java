package dao;

import dto.AnnuncioDTO;
import dto.UtenteDTO;
import dto.OggettoDTO;
import dto.DB_Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AnnuncioDAO {

    private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }

    public void inserisci(AnnuncioDTO dto) {
        validateForInsert(dto); // <-- validazione interna

        String sql = """
            INSERT INTO Annuncio
            (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato,
             Tipo, PrezzoVendita, IdOggettoOfferto, NoteRegalo, FK_Oggetto, FK_Utente)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dto.getIdAnnuncio());
            ps.setString(2, dto.getTitolo());
            ps.setString(3, dto.getDescrizione());
            ps.setDate(4, java.sql.Date.valueOf(dto.getDataPubblicazione()));
            ps.setString(5, dto.getCategoria().name());
            ps.setString(6, dto.getStato().name());
            ps.setString(7, dto.getTipoAnnuncio().name());

            // Campi condizionali
            if (dto.getTipoAnnuncio() == TipoAnnuncio.VENDITA) {
                ps.setBigDecimal(8, dto.getPrezzoVendita());
            } else {
                ps.setNull(8, java.sql.Types.DECIMAL);
            }

            if (dto.getTipoAnnuncio() == TipoAnnuncio.SCAMBIO) {
                ps.setString(9, dto.getIdOggettoOfferto());
            } else {
                ps.setNull(9, java.sql.Types.VARCHAR);
            }

            if (dto.getTipoAnnuncio() == TipoAnnuncio.REGALO) {
                ps.setString(10, dto.getNoteRegalo());
            } else {
                ps.setNull(10, java.sql.Types.VARCHAR);
            }

            ps.setString(11, dto.getIdOggetto());
            ps.setString(12, dto.getMatricola());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore inserimento annuncio " + dto.getId(), e);
        }
    }

    private void validateForInsert(AnnuncioDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto null");
        if (isBlank(dto.getTitolo())) throw new IllegalArgumentException("Titolo obbligatorio");
        if (dto.getCategoria() == null) throw new IllegalArgumentException("Categoria obbligatoria");
        if (dto.getTipoAnnuncio() == null) throw new IllegalArgumentException("Tipo obbligatorio");
        if (dto.getIdOggetto() == null) throw new IllegalArgumentException("Oggetto mancante");
        if (dto.getMatricolaUtente() == null) throw new IllegalArgumentException("Utente mancante");
        if (dto.getDataPubblicazione() == null) throw new IllegalArgumentException("Data pubblicazione mancante");

        switch (dto.getTipoAnnuncio()) {
            case VENDITA -> {
                if (dto.getPrezzoVendita() == null || dto.getPrezzoVendita().signum() < 0)
                    throw new IllegalArgumentException("Prezzo non valido");
            }
            case SCAMBIO -> {
                if (dto.getIdOggettoOfferto() == null)
                    throw new IllegalArgumentException("Oggetto offerto mancante per scambio");
            }
            case REGALO -> {
                if (dto.getPrezzoVendita() != null)
                    throw new IllegalArgumentException("Il prezzo deve essere null per un regalo");
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ... findById(), findByTipo(), listAttivi(), aggiornaStato(), elimina() come prima
}
