package dto;



public class OggettoDTO {

    private String ID_Oggetto;              
    private String nome;             
    private int numProprietari;      
    private String condizione;       
    private String dimensione;       
    private Float peso;            
    private UtenteDTO proprietario;  


    public OggettoDTO(String ID_Oggetto, String nome, int numProprietari, String condizione, String dimensione, Float peso, UtenteDTO proprietario) {
        this.ID_Oggetto = ID_Oggetto;
        this.nome = nome;
        this.numProprietari = numProprietari;
        this.condizione = condizione;
        this.dimensione = dimensione;
        this.peso = peso;
        this.proprietario = proprietario;
    }

    public String getIdOggetto() { 
        return ID_Oggetto; 
    }

    public void setIdOggetto(String ID_Oggetto) {
        this.ID_Oggetto = ID_Oggetto;
    }

    public String getNomeOggetto() {
        return nome; 
    }

    public void setNome(String nome) { 
        this.nome = nome;
    }

    public int getNumProprietari() { 
        return numProprietari; 
    }

    public void setNumProprietari(int numProprietari) { 
        this.numProprietari = numProprietari; 
    }

    public String getCondizione() { 
        return condizione; 
    }
    
    public void setCondizione(String condizione) { 
        this.condizione = condizione; 
    }

    public String getDimensione() { 
        return dimensione; 
    }
    
    public void setDimensione(String dimensione) { 
        this.dimensione = dimensione; 
    }

    public Float getPeso() { 
        return peso; 
    }
    
    public void setPeso(Float peso) { 
        this.peso = peso; 
    }

    public UtenteDTO getProprietario() { 
        return proprietario; 
    }
    
    public void setProprietario(UtenteDTO proprietario) { 
        this.proprietario = proprietario;
    }



   
}
