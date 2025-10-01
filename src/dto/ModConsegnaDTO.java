package dto;

import java.time.LocalDate;

public class ModConsegnaDTO {
    private String ID_Annuncio;   
    private String sedeUni;         
    private String note;            
    private String fasciaOraria;    
    private LocalDate data;             

    public ModConsegnaDTO(String annuncio, String sedeUni, String note, String fasciaOraria, LocalDate data) {
        this.ID_Annuncio = annuncio;
        this.sedeUni = sedeUni;
        this.note = note;
        this.fasciaOraria = fasciaOraria;
        this.data = data;
    }

    public String getAnnuncio() {
        return ID_Annuncio;
    }

    public void setAnnuncio(String annuncio) {
        this.ID_Annuncio = annuncio;
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}