package dao.interf;

import dto.OffertaDTO;
import dto.StatoOffertaDTO;

import java.sql.SQLException;
import java.util.List;

public interface OffertaDAOinterf {

	public void insertOfferta(OffertaDTO offerta) throws SQLException;

	public OffertaDTO getOffertaById(String idOfferta) throws SQLException;

	public List<OffertaDTO> getOfferteByAnnuncio(String idAnnuncio) throws SQLException;

	public List<OffertaDTO> getOfferteByUtente(String matricolaOfferente) throws SQLException;

	public boolean updateStatoOfferta(String idOfferta, StatoOffertaDTO statoCorrente, StatoOffertaDTO statoNuovo) throws SQLException;

	public boolean updateOfferta(OffertaDTO offerta) throws SQLException;

	public boolean deleteOffertaById(String idOfferta) throws SQLException;
}
