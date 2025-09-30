package dto;


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


public class UtenteDTO {
    
    private String nome;
    private String cognome;
    private String email;
    private String matricola;
    private String username;
    private String password;
    private String dataNascita;
    private String genere;
    private ArrayList<AnnuncioDTO> annunciCreati;
    private ArrayList<OggettoDTO> oggettiPosseduti;


    public UtenteDTO(String nome,String cognome, String email, String matricola,String username,String password,String dataNascita,String genere) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.matricola = matricola;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.genere = genere;
        this.annunciCreati = new ArrayList<>();
        this.oggettiPosseduti = new ArrayList<>();

    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public ArrayList<AnnuncioDTO> getAnnunci(){
        return annunciCreati;
    }


    public ArrayList<OggettoDTO> getOggetti(){
        return oggettiPosseduti;
    }




}
