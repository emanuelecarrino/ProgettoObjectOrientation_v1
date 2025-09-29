package main;

import java.sql.*;
import java.util.ArrayList;

import DAOs.UtenteDAOimpl;
import DAOs.UtenteDAOinterf;

public class Controller {
    private ConnessioneDb databaseconnection;
    private UtenteDAOinterf utenteDAO = new UtenteDAOimpl(this);

    public static void main(String[] args) {
        new Controller();
    }

    public Controller() {
        databaseconnection = ConnessioneDb.getInstance();
        databaseconnection.generaDatabase();
    }

    // DAO wrapper methods (stile esempio)
    public boolean registraUtente(String nome, String cognome, String matricola, String dataNascita, String genere, String username, String password) {
        return utenteDAO.registra(nome, cognome, matricola, dataNascita, genere, username, password);
    }

    public ArrayList<String> getUtenteInfo(String matricola) { return utenteDAO.getUtenteInfo(matricola); }

    public String prossimoNumeroMatricola() { return utenteDAO.massimoMatricola(); }

    public void dbConnect() { databaseconnection.dbConnect(); }

    public void dbClose(ResultSet result, Statement statement, Connection dbConnect) {
        databaseconnection.dbClose(result, statement, dbConnect);
    }

    public ResultSet getResult() { return databaseconnection.getResult(); }
    public Statement getStatement() { return databaseconnection.getStatement(); }
    public Connection getDbConnect() { return databaseconnection.getDbConnect(); }
}
