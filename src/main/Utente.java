package main;

import java.util.Objects;

public class Utente {
    private String nome;
    private String cognome;
    private String matricola;
    private String username;
    private String password;
    private String dataNascita;
    private String genere;

    public Utente() {
    }

    public Utente(String nome,String cognome,String matricola,String username,String password,String dataNascita,String genere) {
        this.nome = nome;
        this.cognome = cognome;
        this.matricola = matricola;
        this.username = username;
        this.password = password;
        this.dataNascita = dataNascita;
        this.genere = genere;
    }
        //cacchino
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

}
