package dao.interf;

import dto.UtenteDTO;

import java.sql.SQLException;

public interface UtenteDAOinterf {

	public void insertUtente(UtenteDTO utente) throws SQLException;

	public UtenteDTO getUtenteByUsername(String username) throws SQLException;

	public UtenteDTO getUtenteByMatricola(String matricola) throws SQLException;

	public UtenteDTO getUtenteByEmail(String email) throws SQLException;

	public boolean updateUtente(UtenteDTO utente) throws SQLException;

	public boolean deleteUtenteByMatricola(String matricola) throws SQLException;
}
