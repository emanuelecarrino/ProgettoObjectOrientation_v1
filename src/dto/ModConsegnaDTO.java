package dto;

import java.time.LocalDate;

public class ModConsegnaDTO {
    private String ID_Consegna;
    private String ID_Annuncio;   
    private String sedeUni;         
    private String note;            
    private String fasciaOraria;    
    private LocalDate data;             

    public ModConsegnaDTO(String ID_Consegna, String ID_Annuncio, String sedeUni, String note, String fasciaOraria, LocalDate data) {
        this.ID_Consegna = ID_Consegna;
        this.ID_Annuncio = ID_Annuncio;
        this.sedeUni = sedeUni;
        this.note = note;
        this.fasciaOraria = fasciaOraria;
        this.data = data;
    }


    public String getIdConsegna(){
        return ID_Consegna;
    }

    public void setIdConsegna(String ID_Consegna){
        this.ID_Consegna = ID_Consegna;
    }

    public String getIdAnnuncio() {
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