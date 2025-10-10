package dto;

import dao.UtenteDAO;
import dao.OggettoDAO;
import dao.AnnuncioDAO;
import dao.OffertaDAO;
import dao.ModConsegnaDAO;
import exception.*;

import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controller applicativo: centralizza la gestione delle eccezioni provenienti dai DAO
 * (che propagano SQLException e IllegalArgumentException) e le traduce in eccezioni
 * di dominio custom per i layer superiori (UI / API).
 */


public class Controller {

	private final UtenteDAO utenteDAO = new UtenteDAO();
	private final OggettoDAO oggettoDAO = new OggettoDAO();
	private final AnnuncioDAO annuncioDAO = new AnnuncioDAO();
	private final OffertaDAO offertaDAO = new OffertaDAO();
	private final ModConsegnaDAO modConsegnaDAO = new ModConsegnaDAO();










	// ================== METODI UTENTE  ==================





	// Registrazione nuovo utente con controlli di unicità applicativi prima dell'inserimento
	public void registraNuovoUtente(String nome, String cognome, String email, String matricola, String username, 
                                    String password, String dataNascita, String genere) throws ApplicationException {
	

            try {
			// Controlli base 
			if (isBlank(nome)) throw new ValidationException("Errore su Nome");
			if (isBlank(cognome)) throw new ValidationException("Errore su Cognome");
			if (isBlank(email)) throw new ValidationException("Errore su Email");
			if (isBlank(matricola)) throw new ValidationException("Errore su Matricola");
			if (isBlank(username)) throw new ValidationException("Errore su Username");
			if (isBlank(password)) throw new ValidationException("Errore su Password");
			if (isBlank(dataNascita)) throw new ValidationException("Errore su DataNascita");

			// Unicità lato applicazione 

			if (utenteDAO.getUtenteByUsername(username) != null)
				throw new DuplicateResourceException("Username già esistente");
			if (utenteDAO.getUtenteByEmail(email) != null)
				throw new DuplicateResourceException("Email già esistente");
			if (utenteDAO.getUtenteByMatricola(matricola) != null)
				throw new DuplicateResourceException("Matricola già esistente");

			UtenteDTO nuovo = new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
			utenteDAO.insertUtente(nuovo);
		} catch (DuplicateResourceException | ValidationException e) {
			throw e; 
		} catch (SQLException sql) {
			if (isUniqueViolation(sql)) {
				throw new DuplicateResourceException("Violazione unicità (inserimento)");
			}
			throw new PersistenceException("Errore persistenza registrazione", sql);
		} catch (IllegalArgumentException x) {
			throw new ValidationException(x.getMessage());
		}
	}





	// Login semplice per username + password (o email + password se rilevi @)


	public UtenteDTO login(String userOrEmail, String password) throws ApplicationException {
		try {
			if (isBlank(userOrEmail)) throw new ValidationException("Errore su Username");
			if (isBlank(password)) throw new ValidationException("Errore su Password");
			UtenteDTO utente;
			if (userOrEmail.contains("@")) {
				utente = utenteDAO.getUtenteByEmail(userOrEmail);
			} else {
				utente = utenteDAO.getUtenteByUsername(userOrEmail);
			}
			if (utente == null) throw new AuthenticationException("Credenziali errate");
			if (!password.equals(utente.getPassword())) throw new AuthenticationException("Credenziali errate");
			return utente;
		} catch (AuthenticationException | ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore durante login", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}



	// Aggiornamento profilo utente (verifica unicità per email/username se cambiati)
	public void aggiornaProfilo(UtenteDTO utente) throws ApplicationException {
		try {
			if (utente == null) throw new ValidationException("Utente null");
			if (isBlank(utente.getMatricola())) throw new ValidationException("Errore su Matricola");
			if (isBlank(utente.getNome())) throw new ValidationException("Errore su Nome");
			if (isBlank(utente.getCognome())) throw new ValidationException("Errore su Cognome");
			if (isBlank(utente.getEmail())) throw new ValidationException("Errore su Email");
			if (isBlank(utente.getUsername())) throw new ValidationException("Errore su Username");
			if (isBlank(utente.getPassword())) throw new ValidationException("Errore su Password");
			if (utente.getDataNascita() == null) throw new ValidationException("Errore su DataNascita");
			UtenteDTO originale = utenteDAO.getUtenteByMatricola(utente.getMatricola());
			if (originale == null) throw new NotFoundException("Utente non trovato");

			// Se email cambiata
			if (!originale.getEmail().equalsIgnoreCase(utente.getEmail())) {
				UtenteDTO conflict = utenteDAO.getUtenteByEmail(utente.getEmail());
				if (conflict != null) throw new DuplicateResourceException("Email già esistente");
			}
			// Se username cambiato
			if (!originale.getUsername().equalsIgnoreCase(utente.getUsername())) {
				UtenteDTO conflict = utenteDAO.getUtenteByUsername(utente.getUsername());
				if (conflict != null) throw new DuplicateResourceException("Username già esistente");
			}

			boolean aggiornamentoUtenteRiuscito = utenteDAO.updateUtente(utente);
			if (!aggiornamentoUtenteRiuscito) throw new NotFoundException("Utente non trovato");
		} catch (DuplicateResourceException | ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			if (isUniqueViolation(sql)) throw new DuplicateResourceException("Violazione unicità (update)");
			throw new PersistenceException("Errore aggiornamento profilo", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	public void aggiornaProfilo(String matricola, String nome, String cognome, String email, String username, String password, String dataNascita, String genere) throws ApplicationException {
		UtenteDTO u = new UtenteDTO(nome, cognome, email, matricola, username, password, dataNascita, genere);
		aggiornaProfilo(u);
	}

	// Eliminazione utente
	public void eliminaUtente(String matricola) throws ApplicationException {
		try {
			if (isBlank(matricola)) throw new ValidationException("Errore su Matricola");
			boolean eliminazioneUtenteRiuscita = utenteDAO.deleteUtenteByMatricola(matricola.trim());
			if (!eliminazioneUtenteRiuscita) throw new NotFoundException("Utente non trovato");
		} catch (NotFoundException | ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore eliminazione utente", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}



	// Cambio password (richiede password corrente corretta)
	public void cambiaPassword(String username, String vecchiaPassword, String nuovaPassword) throws ApplicationException {
		try {
			if (isBlank(username)) throw new ValidationException("Errore su Username");
			if (isBlank(vecchiaPassword)) throw new ValidationException("Errore su Password");
			if (isBlank(nuovaPassword)) throw new ValidationException("Errore su Password");
			UtenteDTO utente = utenteDAO.getUtenteByUsername(username);
			if (utente == null) throw new NotFoundException("Utente non trovato");
			if (!utente.getPassword().equals(vecchiaPassword)) throw new AuthenticationException("Credenziali errate");
			utente.setPassword(nuovaPassword);
			utenteDAO.updateUtente(utente);
		} catch (AuthenticationException | NotFoundException | ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore cambio password", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	

	private boolean isBlank(String s){
		return s == null || s.trim().isEmpty();
	}

	// Riconosce violazioni di unicità (PostgreSQL codice 23505)
	private boolean isUniqueViolation(SQLException ex){
		return "23505".equals(ex.getSQLState());
	}























	// ================== METODI ANNUNCI ==================

	// Crea un nuovo annuncio; per tipo VENDITA il prezzo è obbligatorio e > 0, altrimenti ignorato
	public AnnuncioDTO creaAnnuncio(String titolo, String descrizione, CategoriaAnnuncioDTO categoria,
									TipoAnnuncioDTO tipo, BigDecimal prezzoVendita, String ID_Oggetto,
									String creatore) throws ApplicationException {
		try {
			if (isBlank(titolo)) throw new ValidationException("Errore su Titolo");
			if (categoria == null) throw new ValidationException("Errore su Categoria");
			if (tipo == null) throw new ValidationException("Errore su Tipo");
			if (isBlank(ID_Oggetto)) throw new ValidationException("Errore su FK_Oggetto");
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");

			BigDecimal prezzo = null;
			if (tipo == TipoAnnuncioDTO.Vendita) {
				if (prezzoVendita == null) throw new ValidationException("Errore su PrezzoVendita");
				if (prezzoVendita.signum() <= 0) throw new ValidationException("Errore su PrezzoVendita");
				prezzo = prezzoVendita;
			}

			String descrizionePulita = null;
			if (descrizione != null) {
				descrizionePulita = descrizione.trim();
			}
			AnnuncioDTO nuovo = new AnnuncioDTO(generaIdAnnuncio(), titolo.trim(), descrizionePulita, StatoAnnuncioDTO.Attivo,
			categoria, LocalDate.now(), creatore.trim(),ID_Oggetto.trim(),tipo,prezzo);
			
			annuncioDAO.insertAnnuncio(nuovo);
			return nuovo;
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore persistenza creazione annuncio", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	
	public AnnuncioDTO creaAnnuncio(String titolo, String descrizione, String tipoStr, String categoriaStr, String prezzoStr, String idOggetto, String creatore) throws ApplicationException {
		try {
			if (isBlank(tipoStr)) throw new ValidationException("Errore su Tipo");
			TipoAnnuncioDTO tipoEnum = null;
			for (TipoAnnuncioDTO t : TipoAnnuncioDTO.values()) if (t.name().equalsIgnoreCase(tipoStr.trim())) { tipoEnum = t; break; }
			if (tipoEnum == null) throw new ValidationException("Tipo non valido");

			if (isBlank(categoriaStr)) throw new ValidationException("Errore su Categoria");
			CategoriaAnnuncioDTO catEnum = null;
			for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) if (c.name().equalsIgnoreCase(categoriaStr.trim())) { catEnum = c; break; }
			if (catEnum == null) throw new ValidationException("Categoria non valida");

			java.math.BigDecimal prezzo = null;
			if (tipoEnum == TipoAnnuncioDTO.Vendita) {
				if (prezzoStr == null || prezzoStr.trim().isEmpty()) throw new ValidationException("Prezzo richiesto per Vendita");
				try { prezzo = new java.math.BigDecimal(prezzoStr.trim().replace(",",".")); }
				catch (NumberFormatException nfe) { throw new ValidationException("Formato prezzo non valido"); }
			}

			return creaAnnuncio(titolo, descrizione, catEnum, tipoEnum, prezzo, idOggetto, creatore);
		} catch (ValidationException | NotFoundException | PersistenceException e) {
			throw e;
		}
	}

	// Ricerca per tipo
	public List<AnnuncioDTO> cercaAnnunciPerTipo(TipoAnnuncioDTO tipo) throws ApplicationException {
		try {
			if (tipo == null) throw new ValidationException("Errore su Tipo");
			return annuncioDAO.getAnnunciByTipo(tipo);
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca annunci per tipo", sql);
		}
	}

	// Ricerca per categoria
	public List<AnnuncioDTO> cercaAnnunciPerCategoria(CategoriaAnnuncioDTO categoria) throws ApplicationException {
		try {
			if (categoria == null) throw new ValidationException("Errore su Categoria");
			return annuncioDAO.getAnnunciByCategoria(categoria);
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca annunci per categoria", sql);
		}
	}

	// Ricerca per titolo (LIKE case-insensitive)
	public List<AnnuncioDTO> cercaAnnunciPerTitolo(String testo) throws ApplicationException {
		try {
			if (isBlank(testo)) throw new ValidationException("Errore su Titolo");
			return annuncioDAO.getAnnunciByTitolo(testo);
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca annunci per titolo", sql);
		}
	}


	// Output di tutti gli annunci (per default solo quelli con stato: ATTIVO)

	public List<AnnuncioDTO> visualizzaTuttiAnnunci() throws ApplicationException {
    try {
        return annuncioDAO.getAllAnnunci();
    } catch (SQLException sql) {
        throw new PersistenceException("Errore recupero annunci attivi", sql);
    }
	}

	// Annunci attivi escluso il creatore corrente
	public List<AnnuncioDTO> visualizzaAnnunciAltruiAttivi(String creatoreDaEscludere) throws ApplicationException {
		try {
			if (isBlank(creatoreDaEscludere)) throw new ValidationException("Errore su FK_Utente");
			return annuncioDAO.getAnnunciAttiviEsclusoCreatore(creatoreDaEscludere.trim());
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero annunci altrui attivi", sql); }
	}


	// Ricerca per prezzo massimo (solo annunci di tipo VENDITA verranno restituiti)
	public List<AnnuncioDTO> cercaAnnunciPerPrezzoMax(BigDecimal prezzoMax) throws ApplicationException {
		try {
			if (prezzoMax == null) throw new ValidationException("Errore su PrezzoVendita");
			if (prezzoMax.signum() < 0) throw new ValidationException("Errore su PrezzoVendita");
			return annuncioDAO.getAnnunciByPrezzoMax(prezzoMax);
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca annunci per prezzo", sql);
		}
	}

	// Ricerca per creatore
	public List<AnnuncioDTO> cercaAnnunciPerCreatore(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			return annuncioDAO.getAnnunciByCreatore(creatore.trim());
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca annunci per creatore", sql);
		}
	}

	// Aggiornamento annuncio (non consente variazione tipo o oggetto)
	public AnnuncioDTO aggiornaAnnuncio(String ID_Annuncio, String nuovoTitolo, String nuovaDescrizione,
										CategoriaAnnuncioDTO nuovaCategoria, StatoAnnuncioDTO nuovoStato,
										BigDecimal nuovoPrezzo) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			if (isBlank(nuovoTitolo)) throw new ValidationException("Errore su Titolo");
			if (nuovaCategoria == null) throw new ValidationException("Errore su Categoria");
			if (nuovoStato == null) throw new ValidationException("Errore su Stato");

			AnnuncioDTO esistente = annuncioDAO.getAnnuncioById(ID_Annuncio.trim());
			if (esistente == null) throw new NotFoundException("Annuncio non trovato");

			TipoAnnuncioDTO tipo = esistente.getTipoAnnuncio();
			String ID_Oggetto = esistente.getIdOggetto();
			String creatore = esistente.getCreatore();
			LocalDate dataPub = esistente.getDataPubblicazione();

			BigDecimal prezzoFinale = null;
			if (tipo == TipoAnnuncioDTO.Vendita) {
				if (nuovoPrezzo != null) {
					if (nuovoPrezzo.signum() <= 0) throw new ValidationException("Errore su PrezzoVendita");
					prezzoFinale = nuovoPrezzo;
				} else {
					prezzoFinale = esistente.getPrezzoVendita();
				}
			}

			String nuovaDescrizionePulita = null;
			if (nuovaDescrizione != null) {
				nuovaDescrizionePulita = nuovaDescrizione.trim();
			}
			AnnuncioDTO utente = new AnnuncioDTO(
					esistente.getIdAnnuncio(),
					nuovoTitolo.trim(),
					nuovaDescrizionePulita,
					nuovoStato,
					nuovaCategoria,
					dataPub,
					creatore,
					ID_Oggetto,
					tipo,
					prezzoFinale
			);

			boolean aggiornamentoAnnuncioRiuscito = annuncioDAO.updateAnnuncio(utente);
			if (!aggiornamentoAnnuncioRiuscito) throw new NotFoundException("Annuncio non trovato");
			return utente;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento annuncio", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	// Wrapper UI per aggiornare un annuncio usando stringhe/valori primitivi
	public AnnuncioDTO aggiornaAnnuncio(String ID_Annuncio, String nuovoTitolo, String nuovaDescrizione,
									String categoriaStr, String statoStr, String prezzoStr) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			if (isBlank(nuovoTitolo)) throw new ValidationException("Errore su Titolo");
			AnnuncioDTO esistente = annuncioDAO.getAnnuncioById(ID_Annuncio.trim());
			if (esistente == null) throw new NotFoundException("Annuncio non trovato");

			CategoriaAnnuncioDTO categoria = null;
			for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) if (c.name().equalsIgnoreCase(categoriaStr)) { categoria = c; break; }
			if (categoria == null) throw new ValidationException("Categoria non valida");

			StatoAnnuncioDTO stato = null;
			for (StatoAnnuncioDTO s : StatoAnnuncioDTO.values()) if (s.name().equalsIgnoreCase(statoStr)) { stato = s; break; }
			if (stato == null) throw new ValidationException("Stato non valido");

			java.math.BigDecimal prezzo = null;
			if (esistente.getTipoAnnuncio() == TipoAnnuncioDTO.Vendita) {
				if (prezzoStr != null && !prezzoStr.trim().isEmpty() && !"-".equals(prezzoStr.trim())) {
					try {
						prezzo = new java.math.BigDecimal(prezzoStr.trim().replace(',', '.'));
					} catch (NumberFormatException nfe) {
						throw new ValidationException("Formato prezzo non valido");
					}
				}
			}

			return aggiornaAnnuncio(ID_Annuncio.trim(), nuovoTitolo, nuovaDescrizione, categoria, stato, prezzo);
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento annuncio (UI)", sql);
		}
	}

	

	// Eliminazione annuncio per ID
	public void eliminaAnnuncio(String ID_Annuncio) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			boolean eliminazioneAnnuncioRiuscita = annuncioDAO.deleteAnnuncioById(ID_Annuncio.trim());
			if (!eliminazioneAnnuncioRiuscita) throw new NotFoundException("Annuncio non trovato");
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore eliminazione annuncio", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	private String generaIdAnnuncio(){
		return generaIdSequenziale("ANN-");
	}



















	// ================== METODI OGGETTO ==================

	public OggettoDTO creaOggetto(String nome, Integer numProprietari, String condizioni, String dimensione, Float peso, String proprietario) throws ApplicationException {
		try {
			if (isBlank(nome)) throw new ValidationException("Errore su Nome");
			if (isBlank(proprietario)) throw new ValidationException("Errore su FK_Utente");
			if (numProprietari == null || numProprietari < 1) throw new ValidationException("Errore su numProprietari (min 1)");
			if (isBlank(condizioni)) throw new ValidationException("Errore su Condizioni");
			if (isBlank(dimensione)) throw new ValidationException("Errore su Dimensione");
			if (peso != null && peso < 0) throw new ValidationException("Errore su Peso_Kg");
			OggettoDTO nuovo = new OggettoDTO(generaIdOggetto(), nome.trim(), numProprietari, condizioni, dimensione, peso, proprietario.trim());
			oggettoDAO.insertOggetto(nuovo);
			return nuovo;
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) { 
			throw new PersistenceException("Errore creazione oggetto", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	public OggettoDTO aggiornaOggetto(String ID_Oggetto, String nuovoNome, Integer numProprietari, String condizioni, String dimensione, Float peso) throws ApplicationException {
		try {
			if (isBlank(ID_Oggetto)) throw new ValidationException("Errore su ID_Oggetto");
			OggettoDTO esistente = oggettoDAO.getOggettiById(ID_Oggetto.trim());
			if (esistente == null) throw new NotFoundException("Oggetto non trovato");
			if (isBlank(nuovoNome)) throw new ValidationException("Errore su Nome");
			if (numProprietari == null || numProprietari < 1) throw new ValidationException("Errore su numProprietari (min 1)");
			if (isBlank(condizioni)) throw new ValidationException("Errore su Condizioni");
			if (isBlank(dimensione)) throw new ValidationException("Errore su Dimensione");
			if (peso != null && peso < 0) throw new ValidationException("Errore su Peso_Kg");
			OggettoDTO oggettoAggiornato = new OggettoDTO(esistente.getIdOggetto(), nuovoNome.trim(), numProprietari, condizioni, dimensione, peso, esistente.getProprietario());
			boolean aggiornamentoOggettoRiuscito = oggettoDAO.updateOggetto(oggettoAggiornato);
			if (!aggiornamentoOggettoRiuscito) throw new NotFoundException("Oggetto non trovato");
			return oggettoAggiornato;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento oggetto", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	public void eliminaOggetto(String ID_Oggetto) throws ApplicationException {
		try {
			if (isBlank(ID_Oggetto)) throw new ValidationException("Errore su ID_Oggetto");
			boolean eliminazioneOggettoRiuscita = oggettoDAO.deleteOggettoById(ID_Oggetto.trim());
			if (!eliminazioneOggettoRiuscita) throw new NotFoundException("Oggetto non trovato");
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore eliminazione oggetto", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	public List<OggettoDTO> cercaOggettiPerProprietario(String proprietario) throws ApplicationException {
		try {
			if (isBlank(proprietario)) throw new ValidationException("Errore su FK_Utente");
			return oggettoDAO.getOggettiByPropr(proprietario.trim());
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca oggetti per proprietario", sql);
		}
	}

	public List<OggettoDTO> cercaOggettiPerNome(String nome) throws ApplicationException {
		try {
			if (isBlank(nome)) throw new ValidationException("Errore su Nome");
			return oggettoDAO.getOggettiByNome(nome.trim());
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca oggetti per nome", sql);
		}
	}

	public OggettoDTO trovaOggettoPerId(String ID_Oggetto) throws ApplicationException {
		try {
			if (isBlank(ID_Oggetto)) throw new ValidationException("Errore su ID_Oggetto");
			OggettoDTO oggettoRecuperato = oggettoDAO.getOggettiById(ID_Oggetto.trim());
			if (oggettoRecuperato == null) throw new NotFoundException("Oggetto non trovato");
			return oggettoRecuperato;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero oggetto", sql);
		}
	}

	public List<OggettoDTO> ordinaOggettiPerPeso(String direzione) throws ApplicationException {
		try {
			return oggettoDAO.orderOggettiByPeso(direzione);
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ordinamento oggetti per peso", sql);
		}
	}

	public List<OggettoDTO> ordinaOggettiPerNumeroProprietari() throws ApplicationException {
		try {
			return oggettoDAO.orderOggettiByNumPropr();
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ordinamento oggetti per numero proprietari", sql);
		}
	}

	private String generaIdOggetto(){
		return generaIdSequenziale("OGG-");
	}
















	// ================== METODI MODCONSEGNA ==================

	public ModConsegnaDTO creaModConsegna(String ID_Annuncio, String sedeUni, String note, String fasciaOraria, LocalDate data) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			if (isBlank(sedeUni)) throw new ValidationException("Errore su SedeUni");
			if (isBlank(fasciaOraria)) throw new ValidationException("Errore su FasciaOraria");
			if (data == null) throw new ValidationException("Errore su Data");
			// opzionale: verifica esistenza annuncio
			AnnuncioDTO annuncio = annuncioDAO.getAnnuncioById(ID_Annuncio.trim());
			if (annuncio == null) throw new NotFoundException("Annuncio non trovato");
			// opzionale: se vuoi impedire più modalità per stesso annuncio
			// if (modConsegnaDAO.getConsegnaByAnnuncio(ID_Annuncio.trim()) != null) throw new ValidationException("Consegna già definita");
			if (!ID_COUNTERS.containsKey("CON-")) {
				String ultimoIdConsegna = modConsegnaDAO.getUltimoIdConsegna();
				seedCounterFromExistingId("CON-", ultimoIdConsegna);
			}
			String ID_Consegna = generaIdConsegna();
			String notePulite = null;
			if (note != null) {
				String trimmedNote = note.trim();
				if (!trimmedNote.isEmpty()) {
					notePulite = trimmedNote;
				}
			}
			ModConsegnaDTO nuovaConsegna = new ModConsegnaDTO(ID_Consegna, ID_Annuncio.trim(), sedeUni.trim(), notePulite, fasciaOraria.trim(), data);
			modConsegnaDAO.insertModConsegna(nuovaConsegna);
			return nuovaConsegna;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore creazione consegna", sql);
		}
	}

	public ModConsegnaDTO trovaConsegnaPerId(String ID_Consegna) throws ApplicationException {
		try {
			if (isBlank(ID_Consegna)) throw new ValidationException("Errore su ID_Consegna");
			ModConsegnaDTO consegna = modConsegnaDAO.getConsegnaById(ID_Consegna.trim());
			if (consegna == null) throw new NotFoundException("Consegna non trovata");
			return consegna;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero consegna", sql);
		}
	}

	public ModConsegnaDTO trovaConsegnaPerAnnuncio(String ID_Annuncio) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			ModConsegnaDTO consegna = modConsegnaDAO.getConsegnaByAnnuncio(ID_Annuncio.trim());
			if (consegna == null) throw new NotFoundException("Consegna non trovata");
			return consegna;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero consegna", sql);
		}
	}

	public ModConsegnaDTO aggiornaConsegna(String ID_Consegna, String nuovaSedeUni, String nuoveNote, String nuovaFasciaOraria, LocalDate nuovaData) throws ApplicationException {
		try {
			if (isBlank(ID_Consegna)) throw new ValidationException("Errore su ID_Consegna");
			ModConsegnaDTO esistente = modConsegnaDAO.getConsegnaById(ID_Consegna.trim());
			if (esistente == null) throw new NotFoundException("Consegna non trovata");
			if (isBlank(nuovaSedeUni)) throw new ValidationException("Errore su SedeUni");
			if (isBlank(nuovaFasciaOraria)) throw new ValidationException("Errore su FasciaOraria");
			if (nuovaData == null) throw new ValidationException("Errore su Data");
			String nuoveNoteTrim = null;
			if (nuoveNote != null) {
				String trimmedNote = nuoveNote.trim();
				if (!trimmedNote.isEmpty()) {
					nuoveNoteTrim = trimmedNote;
				}
			}
			ModConsegnaDTO consegnaAggiornata = new ModConsegnaDTO(
					esistente.getIdConsegna(),
					esistente.getIdAnnuncio(),
					nuovaSedeUni.trim(),
					nuoveNoteTrim,
					nuovaFasciaOraria.trim(),
					nuovaData
			);
			boolean aggiornamentoRiuscito = modConsegnaDAO.updateModConsegna(consegnaAggiornata);
			if (!aggiornamentoRiuscito) throw new NotFoundException("Consegna non trovata");
			return consegnaAggiornata;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento consegna", sql);
		}
	}

	public void eliminaConsegna(String ID_Consegna) throws ApplicationException {
		try {
			if (isBlank(ID_Consegna)) throw new ValidationException("Errore su ID_Consegna");
			boolean eliminazioneRiuscita = modConsegnaDAO.deleteModConsegnaById(ID_Consegna.trim());
			if (!eliminazioneRiuscita) throw new NotFoundException("Consegna non trovata");
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore eliminazione consegna", sql);
		}
	}

	private String generaIdConsegna() {
		return generaIdSequenziale("CON-");
	}






	














	// ================== METODI OFFERTA ==================

	public OffertaDTO creaOfferta(String ID_Annuncio, String offerente, Float prezzo, String commento, TipoOffertaDTO tipo, String ID_OggettoOfferto) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			if (tipo == null) throw new ValidationException("Errore su Tipo");
			AnnuncioDTO annuncio = annuncioDAO.getAnnuncioById(ID_Annuncio.trim());
			if (annuncio == null) throw new NotFoundException("Annuncio non trovato");
			if (annuncio.getStato() != StatoAnnuncioDTO.Attivo) throw new ValidationException("Annuncio non attivo");
			
			// Prevent owner from offering on own announcement (se desiderato)
			if (annuncio.getCreatore() != null && annuncio.getCreatore().equals(offerente)) {
				throw new ValidationException("Utente proprietario non può fare offerta");
			}
			Float prezzoOffertaValore = null;
			if (tipo == TipoOffertaDTO.Vendita) {
				if (prezzo == null) throw new ValidationException("Errore su PrezzoOfferta");
				if (prezzo <= 0f) throw new ValidationException("Errore su PrezzoOfferta");
				prezzoOffertaValore = prezzo;
			} else if (tipo == TipoOffertaDTO.Scambio) {
				if (isBlank(ID_OggettoOfferto)) throw new ValidationException("Errore su ID_OggettoOfferto");
				// opzionale: verificare che l'oggetto esista e appartenga all'offerente
				OggettoDTO oggettoOfferto = oggettoDAO.getOggettiById(ID_OggettoOfferto.trim());
				if (oggettoOfferto == null) throw new NotFoundException("Oggetto offerto non trovato");
				if (!offerente.equals(oggettoOfferto.getProprietario())) throw new AuthenticationException("Non sei proprietario dell'oggetto offerto");
			}
			String commentoNew = null;
			if (commento != null) {
				commentoNew = commento.trim();
			}
			String oggettoOffertoNew = null;
			if (ID_OggettoOfferto != null) {
				oggettoOffertoNew = ID_OggettoOfferto.trim();
			}

			OffertaDTO offertaCreata = new OffertaDTO(generaIdOfferta(), prezzoOffertaValore==null?0f:prezzoOffertaValore, commentoNew, LocalDate.now(), StatoOffertaDTO.Attesa, 
			offerente.trim(), tipo, ID_Annuncio.trim(), oggettoOffertoNew);

			offertaDAO.insertOfferta(offertaCreata);
			return offertaCreata;
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore creazione offerta", sql);
		}
	}

	// La UI deve passare solo String / numeri primitivi senza conoscere gli enum
	public OffertaDTO creaOfferta(String idAnnuncio, String offerente, String tipoStr, String prezzoStr, String commento, String idOggettoOfferto) throws ApplicationException {
		try {
			if (isBlank(tipoStr)) throw new ValidationException("Errore su Tipo");
			TipoOffertaDTO tipo = null;
			// match case-insensitive contro i valori enum
			for (TipoOffertaDTO t : TipoOffertaDTO.values()) {
				if (t.name().equalsIgnoreCase(tipoStr.trim())) { tipo = t; break; }
			}
			if (tipo == null) throw new ValidationException("Tipo offerta non valido");

			Float prezzo = null;
			if (tipo == TipoOffertaDTO.Vendita) {
				if (prezzoStr == null || prezzoStr.trim().isEmpty()) throw new ValidationException("Prezzo richiesto");
				try {
					prezzo = Float.valueOf(prezzoStr.trim());
				} catch (NumberFormatException nfe) {
					throw new ValidationException("Formato prezzo non valido");
				}
			}
			String idOggetto = null;
			if (tipo == TipoOffertaDTO.Scambio) {
				if (idOggettoOfferto == null || idOggettoOfferto.trim().isEmpty()) throw new ValidationException("ID Oggetto offerto richiesto");
				idOggetto = idOggettoOfferto.trim();
			}
			return creaOfferta(idAnnuncio, offerente, prezzo, commento, tipo, idOggetto);
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		}
	}

	public List<OffertaDTO> cercaOffertePerAnnuncio(String ID_Annuncio) throws ApplicationException {
		try {
			if (isBlank(ID_Annuncio)) throw new ValidationException("Errore su ID_Annuncio");
			return offertaDAO.getOfferteByAnnuncio(ID_Annuncio.trim());
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca offerte", sql);
		}
	}

	public List<OffertaDTO> cercaOffertePerUtente(String matricolaOfferente) throws ApplicationException {
		try {
			if (isBlank(matricolaOfferente)) throw new ValidationException("Errore su FK_Utente");
			return offertaDAO.getOfferteByUtente(matricolaOfferente.trim());
		} catch (ValidationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ricerca offerte per utente", sql);
		}
	}

	public OffertaDTO accettaOfferta(String ID_Offerta, String utente) throws ApplicationException {
		try {
			if (isBlank(ID_Offerta)) throw new ValidationException("Errore su ID_Offerta");
			if (isBlank(utente)) throw new ValidationException("Errore su FK_Utente");
			OffertaDTO offertaDaAccettare = offertaDAO.getOffertaById(ID_Offerta.trim());
			if (offertaDaAccettare == null) throw new NotFoundException("Offerta non trovata");
			if (offertaDaAccettare.getStato() != StatoOffertaDTO.Attesa) throw new ValidationException("Offerta non in stato Attesa");
			AnnuncioDTO annuncio = annuncioDAO.getAnnuncioById(offertaDaAccettare.getIdAnnuncio());
			if (annuncio == null) throw new NotFoundException("Annuncio non trovato");
			if (!utente.equals(annuncio.getCreatore())) throw new AuthenticationException("Non autorizzato");
			if (annuncio.getStato() != StatoAnnuncioDTO.Attivo) throw new ValidationException("Annuncio non attivo");
			boolean aggiornamentoStatoAccettazione = offertaDAO.updateStatoOfferta(offertaDaAccettare.getIdOfferta(), StatoOffertaDTO.Attesa, StatoOffertaDTO.Accettata);
			if (!aggiornamentoStatoAccettazione) throw new ValidationException("Offerta già aggiornata");

			return offertaDAO.getOffertaById(offertaDaAccettare.getIdOfferta());
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			String clean = extractSqlMessage(sql);
			if (clean != null && !clean.isBlank()) {
				throw new ValidationException(clean);
			}
			throw new PersistenceException("Errore accettazione offerta", sql);
		}
	}



	public OffertaDTO rifiutaOfferta(String ID_Offerta, String utente) throws ApplicationException {
		try {
			if (isBlank(ID_Offerta)) throw new ValidationException("Errore su ID_Offerta");
			if (isBlank(utente)) throw new ValidationException("Errore su FK_Utente");
			OffertaDTO offertaDaRifiutare = offertaDAO.getOffertaById(ID_Offerta.trim());
			if (offertaDaRifiutare == null) throw new NotFoundException("Offerta non trovata");
			if (offertaDaRifiutare.getStato() != StatoOffertaDTO.Attesa) throw new ValidationException("Offerta non in stato Attesa");
			AnnuncioDTO annuncio = annuncioDAO.getAnnuncioById(offertaDaRifiutare.getIdAnnuncio());
			if (annuncio == null) throw new NotFoundException("Annuncio non trovato");
			if (!utente.equals(annuncio.getCreatore())) throw new AuthenticationException("Non autorizzato");
			boolean aggiornamentoStatoRifiuto = offertaDAO.updateStatoOfferta(offertaDaRifiutare.getIdOfferta(), StatoOffertaDTO.Attesa, StatoOffertaDTO.Rifiutata);
			if (!aggiornamentoStatoRifiuto) throw new ValidationException("Offerta già aggiornata");
			return offertaDAO.getOffertaById(offertaDaRifiutare.getIdOfferta());
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore rifiuto offerta", sql);
		}
	}



	public void ritiraOfferta(String ID_Offerta, String offerente) throws ApplicationException {
		try {
			if (isBlank(ID_Offerta)) throw new ValidationException("Errore su ID_Offerta");
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			OffertaDTO offertaDaRitirare = offertaDAO.getOffertaById(ID_Offerta.trim());
			if (offertaDaRitirare == null) throw new NotFoundException("Offerta non trovata");
			if (!offerente.equals(offertaDaRitirare.getOfferente())) throw new AuthenticationException("Non autorizzato");
			if (offertaDaRitirare.getStato() != StatoOffertaDTO.Attesa) throw new ValidationException("Offerta non ritirabile");
			boolean eliminazioneOffertaRiuscita = offertaDAO.deleteOffertaById(offertaDaRitirare.getIdOfferta());
			if (!eliminazioneOffertaRiuscita) throw new NotFoundException("Offerta non trovata");
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore ritiro offerta", sql);
		}
	}





	// Aggiorna i contenuti dell'offerta (prezzo proposto o oggetto offerto) solo se in stato ATTESA

	public OffertaDTO aggiornaOfferta(String ID_Offerta, String offerente, Float nuovoPrezzo, String nuovoIdOggettoOfferto, String nuovoCommento) throws ApplicationException {
		try {
			if (isBlank(ID_Offerta)) throw new ValidationException("Errore su ID_Offerta");
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			OffertaDTO offertaDaAggiornare = offertaDAO.getOffertaById(ID_Offerta.trim());
			if (offertaDaAggiornare == null) throw new NotFoundException("Offerta non trovata");
			if (!offerente.equals(offertaDaAggiornare.getOfferente())) throw new AuthenticationException("Non autorizzato");
			if (offertaDaAggiornare.getStato() != StatoOffertaDTO.Attesa) throw new ValidationException("Offerta non modificabile");

			// Validazioni per tipo di offerta
			TipoOffertaDTO tipoOfferta = offertaDaAggiornare.getTipo();

			float prezzoFinale = offertaDaAggiornare.getPrezzoOfferta();
			String idOggettoOffertoFinale = offertaDaAggiornare.getIdOggettoOfferto();

			if (tipoOfferta == TipoOffertaDTO.Vendita) {
				if (nuovoPrezzo != null) {
					if (nuovoPrezzo <= 0f) throw new ValidationException("Errore su PrezzoOfferta");
					prezzoFinale = nuovoPrezzo;
				}
			} else if (tipoOfferta == TipoOffertaDTO.Scambio) {
				if (nuovoIdOggettoOfferto != null) {
					if (isBlank(nuovoIdOggettoOfferto)) throw new ValidationException("Errore su ID_OggettoOfferto");
					OggettoDTO oggettoOffertoNuovo = oggettoDAO.getOggettiById(nuovoIdOggettoOfferto.trim());
					if (oggettoOffertoNuovo == null) throw new NotFoundException("Oggetto offerto non trovato");
					if (!offerente.equals(oggettoOffertoNuovo.getProprietario())) throw new AuthenticationException("Non sei proprietario dell'oggetto offerto");
					idOggettoOffertoFinale = nuovoIdOggettoOfferto.trim();
				}
			}

			String commentoModificato = offertaDaAggiornare.getCommento();
			if (nuovoCommento != null) {
				commentoModificato = nuovoCommento.trim();
			}

			OffertaDTO aggiornataDTO = new OffertaDTO(
					offertaDaAggiornare.getIdOfferta(), prezzoFinale, commentoModificato,
					offertaDaAggiornare.getDataOfferta(), offertaDaAggiornare.getStato(),
					offertaDaAggiornare.getOfferente(), offertaDaAggiornare.getTipo(),
					offertaDaAggiornare.getIdAnnuncio(), idOggettoOffertoFinale);

			boolean aggiornamentoContenutoRiuscito = offertaDAO.updateOfferta(aggiornataDTO);
			if (!aggiornamentoContenutoRiuscito) throw new ValidationException("Offerta non aggiornabile");
			return offertaDAO.getOffertaById(aggiornataDTO.getIdOfferta());
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento offerta", sql);
		}
	}





	// Generatore ID Offerta
	private String generaIdOfferta() {
		return generaIdSequenziale("OFF-");
	}

	private String extractSqlMessage(SQLException sql) {
		if (sql == null) return null;
		String message = sql.getMessage();
		if (message == null) return null;
		message = message.strip();
		int newline = message.indexOf('\n');
		if (newline >= 0) {
			message = message.substring(0, newline).strip();
		}
		if (message.startsWith("ERROR:")) {
			message = message.substring("ERROR:".length()).strip();
		} else if (message.startsWith("ERRORE:")) {
			message = message.substring("ERRORE:".length()).strip();
		}
		return message.isEmpty() ? null : message;
	}






	// ================== METODI UTILITY ==================

	private static final java.time.format.DateTimeFormatter DASHBOARD_DATE_FMT = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

	public int contaAnnunciAttiviCreatore(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			int c = 0;
			for (AnnuncioDTO a : miei) if (a.getStato() == StatoAnnuncioDTO.Attivo) c++;
			return c;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore conteggio annunci attivi", sql); }
	}

	public int contaOfferteMieInAttesa(String offerente) throws ApplicationException {
		try {
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim());
			int c=0; for (OffertaDTO o : mie) if (o.getStato()==StatoOffertaDTO.Attesa) c++; return c;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore conteggio offerte mie attesa", sql); }
	}

	public int contaOfferteRicevuteInAttesa(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			int c=0;
			for (AnnuncioDTO a : miei) {
				List<OffertaDTO> offerte = offertaDAO.getOfferteByAnnuncio(a.getIdAnnuncio());
				for (OffertaDTO o : offerte) if (o.getStato()==StatoOffertaDTO.Attesa) c++;
			}
			return c;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore conteggio offerte ricevute attesa", sql); }
	}

	public String dataUltimoAnnuncioCreato(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			java.time.LocalDate latest = null;
			for (AnnuncioDTO a : miei) {
				if (latest == null || a.getDataPubblicazione().isAfter(latest)) latest = a.getDataPubblicazione();
			}
			return latest == null ? "--" : latest.format(DASHBOARD_DATE_FMT);
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore calcolo ultima data annuncio", sql); }
	}

	public List<String> ultimiAnnunci(int limit) throws ApplicationException {
		if (limit <= 0) limit = 5;
		try {
			List<AnnuncioDTO> tutti = annuncioDAO.getAllAnnunci();
			java.util.List<AnnuncioDTO> copia = new java.util.ArrayList<>(tutti);
			copia.sort((a,b) -> b.getDataPubblicazione().compareTo(a.getDataPubblicazione()));
			java.util.List<String> out = new java.util.ArrayList<>();
			int i=0;
			for (AnnuncioDTO a : copia) {
				out.add(a.getTitolo()+" ["+a.getTipoAnnuncio().name()+"] "+a.getStato().name()+" - "+a.getDataPubblicazione().format(DASHBOARD_DATE_FMT));
				if (++i>=limit) break;
			}
			return out;
		} catch (SQLException sql) { throw new PersistenceException("Errore recupero ultimi annunci", sql); }
	}

	public List<String> ultimiAnnunciCreatore(String creatore, int limit) throws ApplicationException {
		if (limit <= 0) limit = 5;
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			miei.sort((a,b) -> b.getDataPubblicazione().compareTo(a.getDataPubblicazione()));
			java.util.List<String> out = new java.util.ArrayList<>();
			int i=0;
			for (AnnuncioDTO a : miei) {
				// Includiamo l'ID all'inizio per permettere operazioni (elimina/modifica) dalla Home
				out.add(a.getIdAnnuncio()+" "+a.getTitolo()+" ["+a.getTipoAnnuncio().name()+"] "+a.getStato().name()+" - "+a.getDataPubblicazione().format(DASHBOARD_DATE_FMT));
				if (++i>=limit) break;
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero ultimi annunci creatore", sql); }
	}

	public List<String> ultimeOfferteUtente(String offerente, int limit) throws ApplicationException {
		if (limit <= 0) limit = 5;
		try {
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim());
			mie.sort((o1,o2) -> o2.getDataOfferta().compareTo(o1.getDataOfferta()));
			java.util.List<String> out = new java.util.ArrayList<>();
			int i=0;
			for (OffertaDTO o : mie) {
				out.add(o.getIdOfferta()+" ["+o.getTipo().name()+"] "+o.getStato().name());
				if (++i>=limit) break;
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero offerte utente", sql); }
	}

	public List<String> offerteDaGestire(String creatore, int limit) throws ApplicationException {
		if (limit <= 0) limit = 5;
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			java.util.List<String> out = new java.util.ArrayList<>();
			for (AnnuncioDTO a : miei) {
				if (a.getStato() != StatoAnnuncioDTO.Attivo) continue;
				if (out.size() >= limit) break;
				List<OffertaDTO> offerte = offertaDAO.getOfferteByAnnuncio(a.getIdAnnuncio());
				for (OffertaDTO o : offerte) {
					if (o.getStato()==StatoOffertaDTO.Attesa) {
						out.add(o.getIdOfferta()+" ["+o.getTipo().name()+"] "+o.getStato().name());
						if (out.size() >= limit) break;
					}
				}
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero offerte da gestire", sql); }
	}


    public String recuperaMatricolaDaUsernameOEmail(String userOrEmail) throws ApplicationException {
        try {
            if (userOrEmail == null || userOrEmail.trim().isEmpty()) throw new ValidationException("Errore su Username");
            if (userOrEmail.contains("@")) {
                UtenteDTO u = utenteDAO.getUtenteByEmail(userOrEmail.trim());
                return u != null ? u.getMatricola() : null;
            } else {
                UtenteDTO u = utenteDAO.getUtenteByUsername(userOrEmail.trim());
                return u != null ? u.getMatricola() : null;
            }
        } catch (ValidationException e) { throw e; }
        catch (SQLException sql) { throw new PersistenceException("Errore recupero matricola", sql); }
    }



	public String recuperaUsernameDaMatricola(String matricola) throws ApplicationException {
		try {
			if (matricola == null || matricola.trim().isEmpty()) throw new ValidationException("Errore su Matricola");
			UtenteDTO u = utenteDAO.getUtenteByMatricola(matricola.trim());
			return u != null ? u.getUsername() : null;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero username", sql); }
	}

	// Restituisce elenco tipi annuncio disponibili come String (ordine fisso)
	public java.util.List<String> elencoTipiAnnuncio() {
		java.util.List<String> out = new java.util.ArrayList<>();
		for (TipoAnnuncioDTO t : TipoAnnuncioDTO.values()) out.add(t.name());
		return out;
	}

	// Restituisce elenco categorie annuncio disponibili come String (ordine fisso)
	public java.util.List<String> elencoCategorieAnnuncio() {
		java.util.List<String> out = new java.util.ArrayList<>();
		for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) out.add(c.name());
		return out;
	}

	// Recupera annunci attivi altrui (esclude creatore) e restituisce stringhe formattate
	public java.util.List<String> annunciAltruiAttiviFormattati(String creatoreDaEscludere) throws ApplicationException {
		try {
			if (creatoreDaEscludere == null || creatoreDaEscludere.trim().isEmpty()) throw new ValidationException("Errore su FK_Utente");
			java.util.List<AnnuncioDTO> elenco = annuncioDAO.getAnnunciAttiviEsclusoCreatore(creatoreDaEscludere.trim());
			java.util.List<String> out = new java.util.ArrayList<>();
			for (AnnuncioDTO a : elenco) {
				out.add(a.getIdAnnuncio() + "|" + a.getTitolo() + "|" + a.getTipoAnnuncio().name() + "|" + a.getCategoria().name() + "|" + (a.getPrezzoVendita()==null?"-":a.getPrezzoVendita()));
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (java.sql.SQLException sql) { throw new PersistenceException("Errore recupero annunci altrui attivi", sql); }
	}

	// Recupera tutti gli annunci attivi (inclusi quelli del creatore) formattati includendo il creatore
	// Formato: ID|Titolo|Tipo|Categoria|Prezzo|Creatore
	public java.util.List<String> annunciAttiviFormattatiInclusiMiei(String creatore) throws ApplicationException {
		try {
			if (creatore == null || creatore.trim().isEmpty()) throw new ValidationException("Errore su FK_Utente");
			java.util.List<AnnuncioDTO> elenco = annuncioDAO.getAllAnnunci(); // se getAllAnnunci() restituisce anche non attivi andrebbe filtrato
			java.util.List<String> out = new java.util.ArrayList<>();
			for (AnnuncioDTO a : elenco) {
				if (a.getStato() != StatoAnnuncioDTO.Attivo) continue; // solo attivi come in precedente versione
				out.add(a.getIdAnnuncio()+"|"+a.getTitolo()+"|"+a.getTipoAnnuncio().name()+"|"+a.getCategoria().name()+"|"+(a.getPrezzoVendita()==null?"-":a.getPrezzoVendita())+"|"+a.getCreatore());
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (java.sql.SQLException sql) { throw new PersistenceException("Errore recupero annunci attivi", sql); }
	}

	public String estraiCreatoreAnnuncio(String record) {
		if (record == null) return null;
		String[] p = record.split("\\|", -1);
		return p.length>=6 ? p[5] : null;
	}

	// Applica filtri in-memory (tipo/categoria) sulle stringhe annuncio restituite da annunciAltruiAttiviFormattati
	public java.util.List<String> filtraAnnunciFormattati(java.util.List<String> annunci, String tipo, String categoria) {
		if (annunci == null) return java.util.Collections.emptyList();
		java.util.List<String> out = new java.util.ArrayList<>();
		for (String r : annunci) {
			// formato: ID|Titolo|Tipo|Categoria|Prezzo
			String[] parts = r.split("\\|", -1);
			if (parts.length < 5) continue;
			String tipoVal = parts[2];
			String catVal = parts[3];
			boolean ok = true;
			if (tipo != null && !tipo.equals("Tutti") && !tipo.equals(tipoVal)) ok = false;
			if (categoria != null && !categoria.equals("Tutte") && !categoria.equals(catVal)) ok = false;
			if (ok) out.add(r);
		}
		return out;
	}

	// Converte record stringa annuncio formattato in una label user-friendly
	public String formatAnnuncioLabel(String record) {
		if (record == null) return "";
		String[] p = record.split("\\|", -1);
		if (p.length < 5) return record;
		String titolo = p[1];
		String tipo = p[2];
		String prezzo = p[4];
		// Mostra prezzo solo per tipo Vendita e se non nullo/"-"
		if ("Vendita".equalsIgnoreCase(tipo) && prezzo != null && !prezzo.equals("-") && !prezzo.isEmpty()) {
			return titolo + " (" + tipo + ") - €" + prezzo;
		} else {
			return titolo + " (" + tipo + ")";
		}
	}

	// Variante che mostra anche il creatore (o "you" se coincide col viewer) in grigio.
	// record formato: ID|Titolo|Tipo|Categoria|Prezzo|Creatore
	public String formatAnnuncioLabelConCreatore(String record, String viewer) {
		if (record == null) return "";
		String[] p = record.split("\\|", -1);
		if (p.length < 6) return formatAnnuncioLabel(record);
		String titolo = p[1];
		String tipo = p[2];
		String prezzo = p[4];
		String creatore = p[5];
		boolean isVenditaConPrezzo = "Vendita".equalsIgnoreCase(tipo) && prezzo != null && !prezzo.equals("-") && !prezzo.isEmpty();
		String prezzoPart = isVenditaConPrezzo ? (" - €"+prezzo) : "";
		String who = (viewer != null && viewer.trim().equalsIgnoreCase(creatore)) ? "you" : creatore;
		// HTML per colorare il creatore in grigio
		return "<html>"+escapeHtml(titolo)+" ("+escapeHtml(tipo)+")"+prezzoPart+" <span style='color:#777777;font-size:11px;'>"+escapeHtml(who)+"</span></html>";
	}

	private String escapeHtml(String s) {
		if (s == null) return "";
		return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
	}

	public String estraiIdAnnuncio(String record) {
        if (record == null) return null;
        String[] p = record.split("\\|", -1);
        return p.length > 0 ? p[0] : null;
    }

	// Restituisce tutti gli stati annuncio disponibili (nomi enum) per uso UI
	public java.util.List<String> elencoStatiAnnuncio() {
		java.util.List<String> out = new java.util.ArrayList<>();
		for (StatoAnnuncioDTO s : StatoAnnuncioDTO.values()) out.add(s.name());
		return out;
	}

	// Fornisce i campi principali dell'offerta per precompilare una dialog di modifica
	// Ordine: tipo, prezzo, commento, idOggettoOfferto, stato, idAnnuncio
	public String[] recuperaOffertaFields(String idOfferta) throws ApplicationException {
		try {
			if (isBlank(idOfferta)) throw new ValidationException("Errore su ID_Offerta");
			OffertaDTO o = offertaDAO.getOffertaById(idOfferta.trim());
			if (o == null) throw new NotFoundException("Offerta non trovata");
			String prezzo = String.valueOf(o.getPrezzoOfferta());
			// per Regalo/Scambio prezzo può essere 0, per assenza usiamo "-"
			if (o.getTipo() != TipoOffertaDTO.Vendita) {
				prezzo = "-";
			}
			return new String[]{
				o.getTipo().name(),
				prezzo,
				o.getCommento(),
				o.getIdOggettoOfferto(),
				o.getStato().name(),
				o.getIdAnnuncio()
			};
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero offerta", sql);
		}
	}

	// Fornisce i campi principali dell'annuncio per precompilare una dialog di modifica
	// Ordine: titolo, descrizione, categoria, stato, tipo, prezzo, dataPubblicazione, creatore, idOggetto
	public String[] recuperaAnnuncioFields(String idAnnuncio) throws ApplicationException {
		try {
			if (isBlank(idAnnuncio)) throw new ValidationException("Errore su ID_Annuncio");
			AnnuncioDTO a = annuncioDAO.getAnnuncioById(idAnnuncio.trim());
			if (a == null) throw new NotFoundException("Annuncio non trovato");
			String prezzo = a.getPrezzoVendita()==null? "-" : a.getPrezzoVendita().toPlainString();
			return new String[]{
				a.getTitolo(),
				a.getDescrizione()==null? "" : a.getDescrizione(),
				a.getCategoria().name(),
				a.getStato().name(),
				a.getTipoAnnuncio().name(),
				prezzo,
				a.getDataPubblicazione()==null? "" : a.getDataPubblicazione().toString(),
				a.getCreatore(),
				a.getIdOggetto()
			};
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero annuncio", sql);
		}
	}

	// La UI deve passare solo String / numeri primitivi senza conoscere gli enum
	public java.util.List<String> oggettiUtenteFormattati(String proprietario) throws ApplicationException {
		try {
			if (isBlank(proprietario)) throw new ValidationException("Errore su FK_Utente");
			java.util.List<OggettoDTO> list = oggettoDAO.getOggettiByPropr(proprietario.trim());
			java.util.List<String> out = new java.util.ArrayList<>();
			for (OggettoDTO o : list) {
				String pesoStr = (o.getPeso()==null?"-":String.valueOf(o.getPeso()));
				out.add(o.getIdOggetto()+"|"+o.getNomeOggetto()+"|"+o.getNumProprietari()+"|"+o.getCondizione()+"|"+o.getDimensione()+"|"+pesoStr);
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (java.sql.SQLException sql) { throw new PersistenceException("Errore recupero oggetti utente", sql); }
	}

	public String formatOggettoLabel(String record) {
		if (record == null) return "";
		String[] p = record.split("\\|", -1);
		if (p.length < 6) return record;
		String nome = p[1];
		String numProp = p[2];
		String cond = p[3];
		String dim = p[4];
		String peso = p[5];
		String extraPeso = (peso == null || peso.equals("-") || peso.isEmpty())? "" : (" - " + peso + "kg");
		return nome + " ("+cond+") dim:"+dim+extraPeso+" | proprietari:"+numProp;
	}

	public String estraiIdOggetto(String record) {
		if (record == null) return null;
		String[] p = record.split("\\|", -1);
		return p.length>0 ? p[0] : null;
	}

	private void seedCounterFromExistingId(String prefix, String existingId) {
		if (existingId == null) return;
		if (!existingId.startsWith(prefix)) return;
		String numericPart = existingId.substring(prefix.length());
		try {
			int numericValue = Integer.parseInt(numericPart);
			ID_COUNTERS.compute(prefix, (p, counter) -> {
				if (counter == null) {
					return new java.util.concurrent.atomic.AtomicInteger(numericValue);
				}
				if (counter.get() < numericValue) {
					counter.set(numericValue);
				}
				return counter;
			});
		} catch (NumberFormatException ignored) {
			// formato ID inatteso: lascio il contatore alla generazione di default
		}
	}

	private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicInteger> ID_COUNTERS = new java.util.concurrent.ConcurrentHashMap<>();
	private String generaIdSequenziale(String prefix) {
		java.util.concurrent.atomic.AtomicInteger counter = ID_COUNTERS.computeIfAbsent(prefix, p -> new java.util.concurrent.atomic.AtomicInteger(0));
		int val = counter.incrementAndGet();
		return prefix + String.format("%05d", val);
	}

	// Recupera UtenteDTO per matricola (comodo wrapper per la UI)
    public UtenteDTO trovaUtentePerMatricola(String matricola) throws ApplicationException {
        try {
            if (isBlank(matricola)) throw new ValidationException("Errore su Matricola");
            UtenteDTO u = utenteDAO.getUtenteByMatricola(matricola.trim());
            if (u == null) throw new NotFoundException("Utente non trovato");
            return u;
        } catch (ValidationException | NotFoundException e) {
            throw e;
        } catch (SQLException sql) {
            throw new PersistenceException("Errore recupero utente", sql);
        }
    }

    /**
     * Restituisce i campi del profilo come array di String (ordine: nome,cognome,email,username,password,dataNascita,genere)
     * Usato dalla UI per evitare di importare il DTO
     */
    public String[] recuperaProfiloFields(String matricola) throws ApplicationException {
        UtenteDTO u = trovaUtentePerMatricola(matricola);
        return new String[]{
            u.getNome(),
            u.getCognome(),
            u.getEmail(),
            u.getUsername(),
            u.getPassword(),
            u.getDataNascita(),
            u.getGenere()
        };
    }

}



