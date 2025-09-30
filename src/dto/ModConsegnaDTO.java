package dto;

import java.util.Date;

public class ModConsegnaDTO {
    private AnnuncioDTO annuncio;   
    private String sedeUni;         
    private String note;            
    private String fasciaOraria;    
    private Date data;             

    public ModConsegnaDTO(AnnuncioDTO annuncio, String sedeUni, String note, String fasciaOraria, Date data) {
        this.annuncio = annuncio;
        this.sedeUni = sedeUni;
        this.note = note;
        this.fasciaOraria = fasciaOraria;
        this.data = data;
    }

    public AnnuncioDTO getAnnuncio() {
        return annuncio;
    }

    public void setAnnuncio(AnnuncioDTO annuncio) {
        this.annuncio = annuncio;
    }

    public String getSedeUni() {
        return sedeUni;
    }

    public void setSedeUni(String sedeUni) {
        this.sedeUni = sedeUni;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFasciaOraria() {
        return fasciaOraria;
    }

    public void setFasciaOraria(String fasciaOraria) {
        this.fasciaOraria = fasciaOraria;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}