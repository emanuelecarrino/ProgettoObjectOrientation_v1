package dto;

import java.time.LocalDate;
import dto.TipoOffertaDTO;

public class OffertaDTO {
    private final String idOfferta;
    private float prezzoOfferta;
    private String commento;
    private final LocalDate dataOfferta;
    private StatoOffertaDTO stato;
    private final String offerente;
    private final TipoOffertaDTO tipo;
    // FK verso Annuncio (assunzione: la tabella Offerta ha FK_Annuncio)
    private final String idAnnuncio;
    // FK opzionale verso l'Oggetto offerto in caso di SCAMBIO
    private final String idOggettoOfferto;

    public OffertaDTO(String idOfferta,
                      float prezzoOfferta,
                      String commento,
                      LocalDate dataOfferta,
                      StatoOffertaDTO stato,
                      String offerente,
                      TipoOffertaDTO tipo,
                      String idAnnuncio,
                      String idOggettoOfferto) {
        this.idOfferta = idOfferta;
        this.prezzoOfferta = prezzoOfferta;
        this.commento = commento;
        this.dataOfferta = dataOfferta;
        this.stato = stato;
        this.offerente = offerente;
        this.tipo = tipo;
        this.idAnnuncio = idAnnuncio;
        this.idOggettoOfferto = idOggettoOfferto;
    }

    public String getIdOfferta(){
        return this.idOfferta;
    }

    public float getPrezzoOfferta() {
        return prezzoOfferta;
    }

    public void setPrezzoOfferta(float prezzoOfferta) {
        this.prezzoOfferta = prezzoOfferta;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public LocalDate getDataOfferta() {
        return dataOfferta;
    }

    public StatoOffertaDTO getStato() {
        return stato;
    }

    public void setStato(StatoOffertaDTO stato) {
        this.stato = stato;
    }

    public String getOfferente() {
        return offerente;
    }

    public TipoOffertaDTO getTipo() {
        return tipo;
    }

    public String getIdAnnuncio() {
        return idAnnuncio;
    }

    public String getIdOggettoOfferto() {
        return idOggettoOfferto;
    }
}