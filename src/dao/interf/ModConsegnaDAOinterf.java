package dao.interf;

import dto.ModConsegnaDTO;

import java.sql.SQLException;

public interface ModConsegnaDAOinterf {

	public void insertModConsegna(ModConsegnaDTO consegna) throws SQLException;

	public ModConsegnaDTO getConsegnaById(String idConsegna) throws SQLException;

	public ModConsegnaDTO getConsegnaByAnnuncio(String idAnnuncio) throws SQLException;

	public boolean updateModConsegna(ModConsegnaDTO consegna) throws SQLException;

	public boolean deleteModConsegnaById(String idConsegna) throws SQLException;

	public String getUltimoIdConsegna() throws SQLException;
}
