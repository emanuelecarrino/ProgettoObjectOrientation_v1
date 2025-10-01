package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import dto.OggettoDTO;

public class AnnuncioDTO {
    private String ID_Annuncio;
    private String titolo;
    private String descrizione;
    private StatoAnnuncioDTO stato;
    private CategoriaAnnuncioDTO categoria;
    private LocalDate dataPubblicazione;
    private String creatore;
    private String ID_Oggetto;
    private TipoAnnuncioDTO tipo;        
    private BigDecimal prezzoVendita;        


    public AnnuncioDTO(String ID_Annuncio, String titolo, String descrizione, StatoAnnuncioDTO stato, CategoriaAnnuncioDTO categoria, LocalDate dataPubblicazione, String creatore, String ID_Oggetto, TipoAnnuncioDTO tipo, BigDecimal prezzoVendita) {
        this.ID_Annuncio = ID_Annuncio;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.stato = stato;
        this.categoria = categoria;
        this.dataPubblicazione = dataPubblicazione;
        this.creatore = creatore;
        this.ID_Oggetto = ID_Oggetto;
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

    public String getCreatore() {
        return creatore;
    }

    public void setCreatore(String creatore) {
        this.creatore = creatore;
    }

    public String getIdOggetto() {
        return ID_Oggetto;
    }

    public void setOggetto(String ID_Oggetto) {
        this.ID_Oggetto = ID_Oggetto;
    }

    public BigDecimal getPrezzoVendita(){
        return prezzoVendita;
    }

    public void setPrezzoVendita(BigDecimal prezzoVendita){
        this.prezzoVendita = prezzoVendita;
    }

    public LocalDate getDataPubblicazione(){
        return dataPubblicazione;
    }

    public void setDataPubblicazione(LocalDate dataPubblicazione){
        this.dataPubblicazione = dataPubblicazione;
    }

    public void setTipoAnnuncio(TipoAnnuncioDTO tipo){
        this.tipo = tipo;
    }

    public TipoAnnuncioDTO getTipoAnnuncio(){
        return tipo;
    }
}



