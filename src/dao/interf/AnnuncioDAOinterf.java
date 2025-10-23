package dao.interf;

import dto.AnnuncioDTO;
import dto.CategoriaAnnuncioDTO;
import dto.StatoAnnuncioDTO;
import dto.TipoAnnuncioDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface AnnuncioDAOinterf {

	public void insertAnnuncio(AnnuncioDTO annuncio) throws SQLException;

	public List<AnnuncioDTO> getAnnunciByTipo(TipoAnnuncioDTO tipo) throws SQLException;

	public List<AnnuncioDTO> getAnnunciByCategoria(CategoriaAnnuncioDTO categoria) throws SQLException;

	public List<AnnuncioDTO> getAnnunciByTitolo(String ricerca) throws SQLException;

	public List<AnnuncioDTO> getAnnunciByPrezzoMax(BigDecimal prezzoMax) throws SQLException;

	public List<AnnuncioDTO> getAnnunciByCreatore(String creatore) throws SQLException;

	public List<AnnuncioDTO> getAnnunciAttiviEsclusoCreatore(String creatore) throws SQLException;

	public AnnuncioDTO getAnnuncioById(String idAnnuncio) throws SQLException;

	public List<AnnuncioDTO> getAllAnnunci() throws SQLException;

	public boolean updateAnnuncio(AnnuncioDTO annuncio) throws SQLException;

	public boolean deleteAnnuncio(AnnuncioDTO annuncio) throws SQLException;

	public boolean deleteAnnuncioById(String idAnnuncio) throws SQLException;
}
