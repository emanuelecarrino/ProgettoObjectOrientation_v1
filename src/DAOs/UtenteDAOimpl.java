package DAOs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import main.Controller;

public class UtenteDAOimpl implements UtenteDAOinterf {
	Controller controller;
	
	public UtenteDAOimpl(Controller controller){
		this.controller = controller;
	}
	
	public boolean registra(String nome, String cognome, String matricola, String dataNascita, String genere, String username, String password) {
		controller.dbConnect();
		ResultSet result = controller.getResult();
		Statement statement = controller.getStatement();
		Connection dbConnect = controller.getDbConnect();
		try {
            String query = "INSERT INTO utente VALUES ('" + matricola + "', '" + nome + "', '" + cognome + "', '" + username + "', '" + password + "', '" + dataNascita + "', '" + genere + "');";
            statement.execute(query);
            controller.dbClose(result, statement, dbConnect);
            return true;
		} catch (SQLException e) {
			controller.dbClose(result, statement, dbConnect);
            return false;
        }
	}
	
	public ArrayList<String> getUtenteInfo(String matricola) {
		ArrayList<String> dati = new ArrayList<String>();
		controller.dbConnect();
		ResultSet result = controller.getResult();
		Statement statement = controller.getStatement();
		Connection dbConnect = controller.getDbConnect();

		try {
            String query = "SELECT u.nome, u.cognome, u.matricola, u.username, u.password, u.data_nascita, u.genere FROM utente AS u WHERE u.matricola = '" + matricola + "'";
            result = statement.executeQuery(query);
            if(result.isBeforeFirst()) {
                while(result.next()) {
                	dati.add(result.getString("nome"));
                	dati.add(result.getString("cognome"));
                	dati.add(result.getString("matricola"));
                	dati.add(result.getString("username"));
                	dati.add(result.getString("password"));
                	dati.add(result.getString("data_nascita"));
                	dati.add(result.getString("genere"));
                }
            }
		} catch (SQLException e) {
            System.out.println(e.getMessage());
        }
		controller.dbClose(result, statement, dbConnect);
		return dati;
	}
	
	public String massimoMatricola() {
    	String matricola = "";
    	controller.dbConnect();
		ResultSet result = controller.getResult();
		Statement statement = controller.getStatement();
		Connection dbConnect = controller.getDbConnect();
        try {
            String query = "SELECT max(matricola) as matr FROM utente";
            result = statement.executeQuery(query);
            while (result.next()) {
            	matricola = result.getString("matr");
            } 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        controller.dbClose(result, statement, dbConnect);
        return matricola;
    }
}
