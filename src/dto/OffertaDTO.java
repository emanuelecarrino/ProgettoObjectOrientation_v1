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
    private final String ID_Annuncio;
    private String ID_OggettoOfferto;

    public OffertaDTO(String idOfferta, float prezzoOfferta, String commento, LocalDate dataOfferta, 
    StatoOffertaDTO stato, String offerente, TipoOffertaDTO tipo, String ID_Annuncio, String ID_OggettoOfferto) {
        this.idOfferta = idOfferta;
        this.prezzoOfferta = prezzoOfferta;
        this.commento = commento;
        this.dataOfferta = dataOfferta;
        this.stato = stato;
        this.offerente = offerente;
        this.tipo = tipo;
        this.ID_Annuncio = ID_Annuncio;
        this.ID_OggettoOfferto = ID_OggettoOfferto;
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
        return ID_Annuncio;
    }

    public String getIdOggettoOfferto() {
        return ID_OggettoOfferto;
    }
}