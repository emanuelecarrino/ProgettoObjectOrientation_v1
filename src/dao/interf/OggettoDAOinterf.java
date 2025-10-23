package dao.interf;

import dto.OggettoDTO;

import java.sql.SQLException;
import java.util.List;

public interface OggettoDAOinterf {

	public void insertOggetto(OggettoDTO oggetto) throws SQLException;

	public List<OggettoDTO> getOggettiByPropr(String proprietario) throws SQLException;

	public List<OggettoDTO> getOggettiByNome(String nome) throws SQLException;

	public OggettoDTO getOggettiById(String id) throws SQLException;

	public List<OggettoDTO> orderOggettiByPeso(String ordine) throws SQLException;

	public List<OggettoDTO> orderOggettiByNumPropr() throws SQLException;

	public boolean updateOggetto(OggettoDTO oggetto) throws SQLException;

	public boolean deleteOggettoById(String idOggetto) throws SQLException;
}
