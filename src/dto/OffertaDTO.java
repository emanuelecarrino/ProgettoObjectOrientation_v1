package dto;

import java.util.Date;

public class OffertaDTO {
    protected String ID_Offerta;
    protected float prezzoOfferta;
    protected String commento;
    protected Date dataOfferta;
    protected StatoOffertaDTO stato;

    public OffertaDTO(String ID_Offerta, float prezzoOfferta, String commento, Date dataOfferta, StatoOffertaDTO stato){
        this.ID_Offerta = ID_Offerta;
        this.prezzoOfferta = prezzoOfferta;
        this.commento = commento;
        this.dataOfferta = dataOfferta;
        this.stato = stato;
    }

    public String getIdOfferta(){
        return this.ID_Offerta;
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

    public StatoOffertaDTO getStato() {
        return stato;
    }

    public void setStato(StatoOffertaDTO stato) {
        this.stato = stato;
    }
    
}