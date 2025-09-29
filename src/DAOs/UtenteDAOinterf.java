package DAOs;

import java.util.ArrayList;


public interface UtenteDAOinterf {
    boolean registra(String nome, String cognome, String matricola, String dataNascita, String genere, String username, String password);
    ArrayList<String> getUtenteInfo(String matricola);
    String massimoMatricola();
}
