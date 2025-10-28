package dto;

import dao.implement.*;
import dao.interf.*;
import exception.*;

import java.sql.SQLException;
import java.util.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Controller: coordina i DAO e traduce errori in eccezioni dell'app.
 */


public class Controller {

	private final UtenteDAOinterf utenteDAO = new UtenteDAO();
	private final OggettoDAOinterf oggettoDAO = new OggettoDAO();
	private final AnnuncioDAOinterf annuncioDAO = new AnnuncioDAO();
	private final OffertaDAOinterf offertaDAO = new OffertaDAO();



















 
	// ================== METODI UTENTE ==================





	// Registra un nuovo utente con semplici controlli e unicità di base
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

	// Login con username/email e password


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

	// Recupera matricola da username o email
	public String recuperaMatricolaDaUsernameOEmail(String userOrEmail) throws ApplicationException {
		try {
			if (userOrEmail == null || userOrEmail.trim().isEmpty()) throw new ValidationException("Errore su Username");
			if (userOrEmail.contains("@")) {
				UtenteDTO u = utenteDAO.getUtenteByEmail(userOrEmail.trim());
				if (u != null) {
					return u.getMatricola();
				}
				return null;
			} else {
				UtenteDTO u = utenteDAO.getUtenteByUsername(userOrEmail.trim());
				if (u != null) {
					return u.getMatricola();
				}
				return null;
			}
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero matricola", sql); }
	}

	// Recupera username partendo dalla matricola
	public String recuperaUsernameDaMatricola(String matricola) throws ApplicationException {
		try {
			if (matricola == null || matricola.trim().isEmpty()) throw new ValidationException("Errore su Matricola");
			UtenteDTO u = utenteDAO.getUtenteByMatricola(matricola.trim());
			if (u != null) {
				return u.getUsername();
			}
			return null;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero username", sql); }
	}

	// Recupera UtenteDTO per matricola (wrapper per UI)
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

	/** Restituisce i campi del profilo come array di String (ordine: nome,cognome,email,username,password,dataNascita,genere) */
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

	private boolean isBlank(String s){
		return s == null || s.trim().isEmpty();
	}

	// Riconosce violazioni di unicità (PostgreSQL codice 23505)
	private boolean isUniqueViolation(SQLException ex){
		return "23505".equals(ex.getSQLState());
	}


	




















	// ================== METODI ANNUNCI ==================

	// Crea un annuncio (per Vendita il prezzo è obbligatorio > 0)
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
			AnnuncioDTO nuovo = new AnnuncioDTO(
				generaIdAnnuncio(), titolo.trim(), descrizionePulita, StatoAnnuncioDTO.Attivo,
				categoria, LocalDate.now(), creatore.trim(), ID_Oggetto.trim(), tipo, prezzo
			);
			
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
			// Conversione semplice: lascia null se non mappabile; i controlli puntuali li fa il metodo core
			TipoAnnuncioDTO tipoEnum = null;
			if (tipoStr != null) {
				for (TipoAnnuncioDTO t : TipoAnnuncioDTO.values()) if (t.name().equalsIgnoreCase(tipoStr.trim())) { tipoEnum = t; break; }
			}
			CategoriaAnnuncioDTO catEnum = null;
			if (categoriaStr != null) {
				for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) if (c.name().equalsIgnoreCase(categoriaStr.trim())) { catEnum = c; break; }
			}
			BigDecimal prezzo = null;
			if (prezzoStr != null && !prezzoStr.trim().isEmpty()) {
				try { prezzo = new BigDecimal(prezzoStr.trim().replace(',', '.')); }
				catch (NumberFormatException nfe) { throw new ValidationException("Formato prezzo non valido"); }
			}

			return creaAnnuncio(titolo, descrizione, catEnum, tipoEnum, prezzo, idOggetto, creatore);
		} catch (ValidationException | PersistenceException e) {
			throw e;
		}
	}



	// Aggiorna un annuncio (tipo e oggetto non cambiano)
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
			AnnuncioDTO aggiornato = new AnnuncioDTO(
				esistente.getIdAnnuncio(), nuovoTitolo.trim(), nuovaDescrizionePulita, nuovoStato,
				nuovaCategoria, dataPub, creatore, ID_Oggetto, tipo, prezzoFinale
			);

					boolean aggiornamentoAnnuncioRiuscito = annuncioDAO.updateAnnuncio(aggiornato);
					if (!aggiornamentoAnnuncioRiuscito) throw new NotFoundException("Annuncio non trovato");
					return aggiornato;
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore aggiornamento annuncio", sql);
		} catch (IllegalArgumentException iae) {
			throw new ValidationException(iae.getMessage());
		}
	}

	// Variante UI: aggiorna annuncio a partire da stringhe
	public AnnuncioDTO aggiornaAnnuncio(String ID_Annuncio, String nuovoTitolo, String nuovaDescrizione,
									String categoriaStr, String statoStr, String prezzoStr) throws ApplicationException {
		try {
				// Conversioni: il metodo core si occupa delle validazioni
				CategoriaAnnuncioDTO categoria = null;
				if (categoriaStr != null) {
					for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) if (c.name().equalsIgnoreCase(categoriaStr.trim())) { categoria = c; break; }
				}
				StatoAnnuncioDTO stato = null;
				if (statoStr != null) {
					for (StatoAnnuncioDTO s : StatoAnnuncioDTO.values()) if (s.name().equalsIgnoreCase(statoStr.trim())) { stato = s; break; }
				}
				BigDecimal prezzo = null;
				if (prezzoStr != null && !prezzoStr.trim().isEmpty() && !"-".equals(prezzoStr.trim())) {
					try {
						prezzo = new BigDecimal(prezzoStr.trim().replace(',', '.'));
					} catch (NumberFormatException nfe) {
						throw new ValidationException("Formato prezzo non valido");
					}
				}

				return aggiornaAnnuncio(ID_Annuncio == null ? null : ID_Annuncio.trim(), nuovoTitolo, nuovaDescrizione, categoria, stato, prezzo);
			} catch (ValidationException e) {
			throw e;
		}
	}

	

	// Elimina annuncio per ID
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


	// Elenco tipi annuncio (stringhe enum)
	public List<String> elencoTipiAnnuncio() {
		List<String> out = new ArrayList<>();
		for (TipoAnnuncioDTO t : TipoAnnuncioDTO.values()) out.add(t.name());
		return out;
	}

	// Elenco categorie annuncio (stringhe enum)
	public List<String> elencoCategorieAnnuncio() {
		List<String> out = new ArrayList<>();
		for (CategoriaAnnuncioDTO c : CategoriaAnnuncioDTO.values()) out.add(c.name());
		return out;
	}

	// Annunci attivi (inclusi i miei) formattati (ID|Titolo|Tipo|Categoria|Prezzo|Creatore)
	public List<String> annunciAttiviFormattatiInclusiMiei(String creatore) throws ApplicationException {
		try {
			List<AnnuncioDTO> elenco = annuncioDAO.getAllAnnunci(); // se getAllAnnunci() restituisce anche non attivi andrebbe filtrato
			List<String> out = new ArrayList<>();
			for (AnnuncioDTO a : elenco) {
				if (a.getStato() != StatoAnnuncioDTO.Attivo) continue; // solo attivi
				String prezzo = "-";
				if (a.getPrezzoVendita() != null) {
					prezzo = a.getPrezzoVendita().toString();
				}
				out.add(a.getIdAnnuncio()+"|"+a.getTitolo()+"|"+a.getTipoAnnuncio().name()+"|"+a.getCategoria().name()+"|"+prezzo+"|"+a.getCreatore());
			}
			return out;
		} catch (java.sql.SQLException sql) { throw new PersistenceException("Errore recupero annunci attivi", sql); }
	}

	// Applica filtri su tipo/categoria a record formattati
	public List<String> filtraAnnunciFormattati(List<String> annunci, String tipo, String categoria) {
		if (annunci == null) return Collections.emptyList();
		List<String> out = new ArrayList<>();
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

	// Estrae l'ID annuncio dal record formattato
	public String estraiIdAnnuncio(String record) {
        if (record == null) return null;
        String[] p = record.split("\\|", -1);
		if (p.length > 0) {
			return p[0];
		}
		return null;
    }

	// Campi principali di un annuncio (per dialog UI)
	public String[] recuperaAnnuncioFields(String idAnnuncio) throws ApplicationException {
		try {
			if (isBlank(idAnnuncio)) throw new ValidationException("Errore su ID_Annuncio");
			AnnuncioDTO a = annuncioDAO.getAnnuncioById(idAnnuncio.trim());
			if (a == null) throw new NotFoundException("Annuncio non trovato");
			String prezzo;
			if (a.getPrezzoVendita() == null) {
				prezzo = "-";
			} else {
				prezzo = a.getPrezzoVendita().toPlainString();
			}
			String descr = a.getDescrizione();
			if (descr == null) descr = "";
			String dataPub = "";
			if (a.getDataPubblicazione() != null) {
				dataPub = a.getDataPubblicazione().toString();
			}
			return new String[]{
				a.getTitolo(),
				descr,
				a.getCategoria().name(),
				a.getStato().name(),
				a.getTipoAnnuncio().name(),
				prezzo,
				dataPub,
				a.getCreatore(),
				a.getIdOggetto()
			};
		} catch (ValidationException | NotFoundException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore recupero annuncio", sql);
		}
	}


	private String generaIdAnnuncio() throws PersistenceException {
		for (int attempts = 0; attempts < MAX_ID_ATTEMPTS; attempts++) {
			String candidate = generaIdConPrefissoENumeri("ANN-");
			if (!existsAnnuncioId(candidate)) {
				return candidate;
			}
		}
		throw new PersistenceException("Impossibile generare ID univoco (Annuncio)", null);
	}


	// ====== Exists helpers per verificare collisioni ID ======
	private boolean existsAnnuncioId(String id) throws PersistenceException {
		try {
			return annuncioDAO.getAnnuncioById(id) != null;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore verifica unicità ID Annuncio", sql);
		}
	}




	// ================== METODI OGGETTO ==================

	// Elenco oggetti di un proprietario
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



	// Trova oggetto per ID
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


	public String trovaNomeOggettoPerId(String ID_Oggetto) throws ApplicationException {
		OggettoDTO o = trovaOggettoPerId(ID_Oggetto);
		return (o != null) ? o.getNomeOggetto() : null;
	}


	// Oggetti dell'utente formattati (ID|Nome|#Prop|Cond|Dim|Peso)
	public List<String> oggettiUtenteFormattati(String proprietario) throws ApplicationException {
		try {
			List<OggettoDTO> list = cercaOggettiPerProprietario(proprietario);
			List<String> out = new ArrayList<>();
			for (OggettoDTO o : list) {
				String pesoStr;
				if (o.getPeso() == null) {
					pesoStr = "-";
				} else {
					pesoStr = String.valueOf(o.getPeso());
				}
				out.add(o.getIdOggetto()+"|"+o.getNomeOggetto()+"|"+o.getNumProprietari()+"|"+o.getCondizione()+"|"+o.getDimensione()+"|"+pesoStr);
			}
			return out;
		} catch (ValidationException e) { throw e; }
	}

	// Label leggibile per un oggetto formattato
	public String formatOggettoLabel(String record) {
		if (record == null) return "";
		String[] p = record.split("\\|", -1);
		if (p.length < 6) return record;
		String nome = p[1];
		String numProp = p[2];
		String cond = p[3];
		String dim = p[4];
		String peso = p[5];
		String extraPeso = "";
		if (peso != null && !peso.equals("-") && !peso.isEmpty()) {
			extraPeso = " - " + peso + "kg";
		}
		return nome + " ("+cond+") dim:"+dim+extraPeso+" | proprietari:"+numProp;
	}

	// Estrae l'ID oggetto dal record formattato
	public String estraiIdOggetto(String record) {
		if (record == null) return null;
		String[] p = record.split("\\|", -1);
		if (p.length > 0) {
			return p[0];
		}
		return null;
	}





	





	// ================== METODI OFFERTA ==================

	// Crea un'offerta su un annuncio
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

			float prezzoOffertaFinale = 0f;
			if (prezzoOffertaValore != null) {
				prezzoOffertaFinale = prezzoOffertaValore;
			}
			OffertaDTO offertaCreata = new OffertaDTO(
				generaIdOfferta(), prezzoOffertaFinale, commentoNew, LocalDate.now(),
				StatoOffertaDTO.Attesa, offerente.trim(), tipo, ID_Annuncio.trim(), oggettoOffertoNew
			);

			offertaDAO.insertOfferta(offertaCreata);
			return offertaCreata;
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore creazione offerta", sql);
		}
	}

	// La UI deve passare solo String / numeri primitivi senza conoscere gli enum
	// Variante UI: crea offerta a partire da stringhe
	public OffertaDTO creaOfferta(String idAnnuncio, String offerente, String tipoStr, String prezzoStr, String commento, String idOggettoOfferto) throws ApplicationException {
		try {
			TipoOffertaDTO tipo = null;
			if (tipoStr != null) {
				for (TipoOffertaDTO t : TipoOffertaDTO.values()) {
					if (t.name().equalsIgnoreCase(tipoStr.trim())) { tipo = t; break; }
				}
			}
			Float prezzo = null;
			if (prezzoStr != null && !prezzoStr.trim().isEmpty()) {
				try {
					prezzo = Float.valueOf(prezzoStr.trim());
				} catch (NumberFormatException nfe) {
					throw new ValidationException("Formato prezzo non valido");
				}
			}
			String idOggetto = (idOggettoOfferto == null || idOggettoOfferto.trim().isEmpty()) ? null : idOggettoOfferto.trim();
			return creaOfferta(idAnnuncio, offerente, prezzo, commento, tipo, idOggetto);
		} catch (ValidationException | NotFoundException | AuthenticationException e) {
			throw e;
		}
	}

	


	

	// Accetta un'offerta (solo creatore annuncio)
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
			throw new PersistenceException("Errore accettazione offerta", sql);
		}
	}



	// Rifiuta un'offerta (solo creatore annuncio)
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



	// Ritira la propria offerta (solo offerente)
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





	// Aggiorna i contenuti dell'offerta se in Attesa
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
				offertaDaAggiornare.getIdOfferta(), prezzoFinale, commentoModificato, offertaDaAggiornare.getDataOfferta(),
				offertaDaAggiornare.getStato(), offertaDaAggiornare.getOfferente(), offertaDaAggiornare.getTipo(),
				offertaDaAggiornare.getIdAnnuncio(), idOggettoOffertoFinale
			);

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

	private String generaIdOfferta() throws PersistenceException {
		for (int attempts = 0; attempts < MAX_ID_ATTEMPTS; attempts++) {
			String candidate = generaIdConPrefissoENumeri("OFF-");
			if (!existsOffertaId(candidate)) {
				return candidate;
			}
		}
		throw new PersistenceException("Impossibile generare ID univoco (Offerta)", null);
	}

	// Genera un ID con prefisso e 5 cifre random (es: ANN-12345)
	private String generaIdConPrefissoENumeri(String prefisso) {
		int num = (int)(Math.random() * 100000); // 0..99999
		return prefisso + String.format("%05d", num);
	}

	

	private boolean existsOffertaId(String id) throws PersistenceException {
		try {
			return offertaDAO.getOffertaById(id) != null;
		} catch (SQLException sql) {
			throw new PersistenceException("Errore verifica unicità ID Offerta", sql);
		}
	}






	// ================== METODI UTILITY ==================

	private static final DateTimeFormatter DASHBOARD_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");
	private static final int MAX_ID_ATTEMPTS = 5000 ;

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
			LocalDate latest = null;
			for (AnnuncioDTO a : miei) {
				if (latest == null || a.getDataPubblicazione().isAfter(latest)) latest = a.getDataPubblicazione();
			}
			if (latest == null) {
				return "--";
			}
			return latest.format(DASHBOARD_DATE_FMT);
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore calcolo ultima data annuncio", sql); }
	}


	// Ultimi annunci del creatore
	public List<String> ultimiAnnunciCreatore(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim()); // già ordinati dal DAO
			List<String> out = new ArrayList<>();
			for (AnnuncioDTO a : miei) {
				out.add(a.getIdAnnuncio()+" "+a.getTitolo()+" ["+a.getTipoAnnuncio().name()+"] "+a.getStato().name()+" - "+a.getDataPubblicazione().format(DASHBOARD_DATE_FMT));
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero ultimi annunci creatore", sql); }
	}

	// Ultime offerte dell'utente
	public List<String> ultimeOfferteUtente(String offerente) throws ApplicationException {
		try {
			if (isBlank(offerente)) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim()); // già ordinate dal DAO
			List<String> out = new ArrayList<>();
			for (OffertaDTO o : mie) {
				out.add(o.getIdOfferta()+" ["+o.getTipo().name()+"] "+o.getStato().name());
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero offerte utente", sql); }
	}

	// Offerte in Attesa da gestire per i propri annunci
	public List<String> offerteDaGestire(String creatore) throws ApplicationException {
		try {
			if (isBlank(creatore)) throw new ValidationException("Errore su FK_Utente");
			List<AnnuncioDTO> miei = annuncioDAO.getAnnunciByCreatore(creatore.trim());
			List<String> out = new ArrayList<>();
			for (AnnuncioDTO a : miei) {
				if (a.getStato() != StatoAnnuncioDTO.Attivo) continue;
				List<OffertaDTO> offerte = offertaDAO.getOfferteByAnnuncio(a.getIdAnnuncio());
				for (OffertaDTO o : offerte) {
					if (o.getStato()==StatoOffertaDTO.Attesa) {
						out.add(o.getIdOfferta()+" ["+o.getTipo().name()+"] "+o.getStato().name());
					}
				}
			}
			return out;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore recupero offerte da gestire", sql); }
	}



	// Fornisce i campi principali dell'offerta per precompilare una dialog di modifica
	// Ordine: tipo, prezzo, commento, idOggettoOfferto, stato, idAnnuncio
	// Campi principali di un'offerta (per dialog UI)
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




	// ================== METODI REPORT / STATISTICHE ==================

	// Totale offerte inviate per tipologia per l'offerente passato
	public Map<String, Integer> reportTotaleOffertePerTipo(String offerente) throws ApplicationException {
		try {
			if (offerente == null || offerente.trim().isEmpty()) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim());
			Map<String, Integer> counts = new LinkedHashMap<>();
			counts.put("Vendita", 0);
			counts.put("Scambio", 0);
			counts.put("Regalo", 0);
			for (OffertaDTO o : mie) {
				String key = o.getTipo().name();
				counts.computeIfPresent(key, (k,v) -> v + 1);
			}
			return counts;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore calcolo offerte per tipologia", sql); }
	}

	// Offerte accettate per tipologia
	public Map<String, Integer> reportOfferteAccettatePerTipo(String offerente) throws ApplicationException {
		try {
			if (offerente == null || offerente.trim().isEmpty()) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim());
			Map<String, Integer> counts = new LinkedHashMap<>();
			counts.put("Vendita", 0);
			counts.put("Scambio", 0);
			counts.put("Regalo", 0);
			for (OffertaDTO o : mie) {
				if (o.getStato() != StatoOffertaDTO.Accettata) continue;
				String key = o.getTipo().name();
				counts.computeIfPresent(key, (k,v) -> v + 1);
			}
			return counts;
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore calcolo offerte accettate per tipologia", sql); }
	}

	// Statistiche prezzo per offerte di Vendita accettate: restituisce [count, min, avg, max]
	public float[] reportStatPrezziOfferteAccettateVendita(String offerente) throws ApplicationException {
		try {
			if (offerente == null || offerente.trim().isEmpty()) throw new ValidationException("Errore su FK_Utente");
			List<OffertaDTO> mie = offertaDAO.getOfferteByUtente(offerente.trim());
			int count = 0;
			float min = Float.MAX_VALUE;
			float max = -Float.MAX_VALUE;
			double sum = 0.0;
			for (OffertaDTO o : mie) {
				if (o.getStato() != StatoOffertaDTO.Accettata) continue;
				if (o.getTipo() != TipoOffertaDTO.Vendita) continue;
				float val = o.getPrezzoOfferta();
				count++;
				sum += val;
				if (val < min) min = val;
				if (val > max) max = val;
			}
			if (count == 0) return new float[]{0f, 0f, 0f, 0f};
			float avg = (float)(sum / count);
			return new float[]{ (float)count, min, avg, max };
		} catch (ValidationException e) { throw e; }
		catch (SQLException sql) { throw new PersistenceException("Errore calcolo statistiche prezzi offerte accettate", sql); }
	}





}



