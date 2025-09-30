package dto;

import java.util.Date;
import java.util.ArrayList;

public class AnnuncioDTO {
    protected String ID_Annuncio;
    protected String titolo;
    protected String descrizione;
    protected StatoAnnuncioDTO stato;
    protected CategoriaAnnuncioDTO categoria;
    protected Date dataPubblicazione;
    protected UtenteDTO creatore;
    protected OggettoDTO oggetto;
    protected ArrayList<OffertaDTO> offerteRicevute;

    public AnnuncioDTO(String titolo, String descrizione, StatoAnnuncioDTO stato, CategoriaAnnuncioDTO categoria, Date dataPubblicazione, UtenteDTO creatore, OggettoDTO oggetto){
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.stato = stato;
        this.categoria = categoria;
        this.dataPubblicazione = dataPubblicazione;
        this.creatore = creatore;
        this.oggetto = oggetto;
        this.offerteRicevute = new ArrayList<>();
    }

    public String getIdAnnuncio() {
        return ID_Annuncio;
    }

    public void setIdAnnuncio(String ID_Annuncio) {
        this.ID_Annuncio = ID_Annuncio;
    }
    
    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

     

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public StatoAnnuncioDTO getStato() {
        return stato;
    }

    public void setStato(StatoAnnuncioDTO stato) {
        this.stato = stato;
    }

    public CategoriaAnnuncioDTO getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaAnnuncioDTO categoria) {
        this.categoria = categoria;
    }

    public UtenteDTO getCreatore() {
        return creatore;
    }

    public void setCreatore(UtenteDTO creatore) {
        this.creatore = creatore;
    }

    public OggettoDTO getOggetto() {
        return oggetto;
    }

    public void setOggetto(OggettoDTO oggetto) {
        this.oggetto = oggetto;
    }

   
}



