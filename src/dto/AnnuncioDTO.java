package dto;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;

public class AnnuncioDTO {
    private String ID_Annuncio;
    private String titolo;
    private String descrizione;
    private StatoAnnuncioDTO stato;
    private CategoriaAnnuncioDTO categoria;
    private Date dataPubblicazione;
    private UtenteDTO creatore;
    private OggettoDTO oggetto;        
    private final ArrayList<OffertaDTO> offerteRicevute;
    private TipoAnnuncioDTO tipo;        
    private float prezzoVendita;        


    public AnnuncioDTO(String ID_Annuncio, String titolo, String descrizione, StatoAnnuncioDTO stato, CategoriaAnnuncioDTO categoria, Date dataPubblicazione, UtenteDTO creatore, OggettoDTO oggetto, TipoAnnuncioDTO tipo, float prezzoVendita) {
        

        this.ID_Annuncio = ID_Annuncio;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.stato = stato;
        this.categoria = categoria;
        this.dataPubblicazione = dataPubblicazione;
        this.creatore = creatore;
        this.oggetto = oggetto;
        this.offerteRicevute = new ArrayList<>();
        this.tipo = tipo;
        this.prezzoVendita = prezzoVendita;

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

    public float getPrezzoVendita(){
        return prezzoVendita;
    }

    public void setPrezzoVendita(float prezzoVendita){
        this.prezzoVendita = prezzoVendita;
    }

    public Date getDataPubblicazione(){
        return dataPubblicazione;
    }

    public void setDataPubblicazione(Date dataPubblicazione){
        this.dataPubblicazione = dataPubblicazione;
    }

    public void setTipoAnnuncio(TipoAnnuncioDTO tipo){
        this.tipo = tipo;
    }

    public TipoAnnuncioDTO getTipoAnnuncio(){
        return tipo;
    }

}



