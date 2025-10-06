---- GETTER  ----

---- PRIMA ----

-- Funzione per ottenere tutti gli annunci attivi di un utente

CREATE OR REPLACE FUNCTION fun_getAnnunciUtente(in_matricola VARCHAR)
RETURNS TABLE (
    id_annuncio VARCHAR,
    titolo VARCHAR,
    descrizione VARCHAR,
    data_pubblicazione DATE,
    categoria categoriaAnnuncio,
    stato statoAnnuncio,
    prezzo_vendita FLOAT,
    tipo tipoAnnuncio,
    nome_oggetto VARCHAR
) AS $$

BEGIN
    RETURN QUERY
    SELECT 
        a.ID_Annuncio,
        a.Titolo,
        a.Descrizione,
        a.DataPubblicazione,
        a.Categoria,
        a.Stato,
        a.prezzoVendita,
        a.Tipo,
        o.Nome
    FROM Annuncio a
    JOIN Oggetto o ON a.FK_Oggetto = o.ID_Oggetto
    WHERE a.FK_Utente = in_matricola
    ORDER BY a.DataPubblicazione DESC;
END;
$$ LANGUAGE plpgsql;



---- SECONDA ----

-- Funzione per ottenere le offerte ricevute da un utente sui suoi annunci

CREATE OR REPLACE FUNCTION fun_getOfferteRicevute(in_matricola VARCHAR)
RETURNS TABLE (
    id_offerta VARCHAR,
    titolo_annuncio VARCHAR,
    commento VARCHAR,
    prezzo_offerta FLOAT,
    data_offerta DATE,
    stato statoOfferta,
    offerente VARCHAR,
    nome_oggetto_offerto VARCHAR
) AS $$

BEGIN
    RETURN QUERY
    SELECT 
        of.ID_Offerta,
        a.Titolo,
        of.Commento,
        of.prezzoOfferta,
        of.DataOfferta,
        of.Stato,
        u.Username,
        o.Nome
    FROM Offerta of
    JOIN Annuncio a ON of.FK_Annuncio = a.ID_Annuncio
    JOIN Utente u ON of.FK_Utente = u.Matricola
    LEFT JOIN Oggetto o ON of.ID_OggettoOfferto = o.ID_Oggetto
    WHERE a.FK_Utente = in_matricola
    ORDER BY of.DataOfferta DESC;
END;
$$ LANGUAGE plpgsql;




---- TERZA ----

-- Funzione per ottenere le offerte fatte da un utente
CREATE OR REPLACE FUNCTION fun_getOfferteFatte(in_matricola VARCHAR)
RETURNS TABLE (
    id_offerta VARCHAR,
    titolo_annuncio VARCHAR,
    commento VARCHAR,
    prezzo_offerta FLOAT,
    data_offerta DATE,
    stato statoOfferta,
    proprietario_annuncio VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        off.ID_Offerta,
        a.Titolo,
        off.Commento,
        off.prezzoOfferta,
        off.DataOfferta,
        off.Stato,
        u.Username
    FROM Offerta off
    JOIN Annuncio a ON off.FK_Annuncio = a.ID_Annuncio
    JOIN Utente u ON a.FK_Utente = u.Matricola
    WHERE off.FK_Utente = in_matricola
    ORDER BY off.DataOfferta DESC;
END;
$$ LANGUAGE plpgsql;



---- QUARTA ----

-- Funzione per ottenere tutti gli oggetti di un utente
CREATE OR REPLACE FUNCTION fun_getOggettiUtente(in_matricola VARCHAR)
RETURNS TABLE (
    id_oggetto VARCHAR,
    nome VARCHAR,
    num_proprietari INTEGER,
    condizioni VARCHAR,
    dimensione VARCHAR,
    peso_kg FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        o.ID_Oggetto,
        o.Nome,
        o.numProprietari,
        o.Condizioni,
        o.Dimensione,
        o.Peso_Kg
    FROM Oggetto o
    WHERE o.FK_Utente = in_matricola
    ORDER BY o.Nome;
END;
$$ LANGUAGE plpgsql;



---- QUINTA ----

-- Funzione per cercare annunci per categoria e tipo
CREATE OR REPLACE FUNCTION fun_cercaAnnunci(
    in_categoria categoriaAnnuncio DEFAULT NULL,
    in_tipo tipoAnnuncio DEFAULT NULL,
    in_prezzo_max FLOAT DEFAULT NULL
)
RETURNS TABLE (
    id_annuncio VARCHAR,
    titolo VARCHAR,
    descrizione VARCHAR,
    data_pubblicazione DATE,
    categoria categoriaAnnuncio,
    prezzo_vendita FLOAT,
    tipo tipoAnnuncio,
    nome_oggetto VARCHAR,
    username_venditore VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.ID_Annuncio,
        a.Titolo,
        a.Descrizione,
        a.DataPubblicazione,
        a.Categoria,
        a.prezzoVendita,
        a.Tipo,
        o.Nome,
        u.Username
    FROM Annuncio a
    JOIN Oggetto o ON a.FK_Oggetto = o.ID_Oggetto
    JOIN Utente u ON a.FK_Utente = u.Matricola
    WHERE a.Stato = 'Attivo'
        AND (in_categoria IS NULL OR a.Categoria = in_categoria)
        AND (in_tipo IS NULL OR a.Tipo = in_tipo)
        AND (in_prezzo_max IS NULL OR a.prezzoVendita IS NULL OR a.prezzoVendita <= in_prezzo_max)
    ORDER BY a.DataPubblicazione DESC;
END;
$$ LANGUAGE plpgsql;






---- SETTER FUNCTIONS ----


---- PRIMA ----

-- Funzione per aggiornare lo stato di un'offerta (accetta/rifiuta)

CREATE OR REPLACE FUNCTION fun_aggiornaStatoOfferta(
    in_id_offerta VARCHAR,
    in_azione VARCHAR,
    in_matricola_proprietario VARCHAR
)
RETURNS BOOLEAN AS $$
DECLARE
    proprietario_annuncio VARCHAR;
    nuovo_stato statoOfferta;
BEGIN
    -- Converte l'azione in stato enum
    IF in_azione = 'Accetta' THEN
        nuovo_stato := 'Accettata';
    ELSIF in_azione = 'R' THEN
        nuovo_stato := 'Rifiutata';
    ELSE
        RAISE EXCEPTION 'Azione non valida. Usa "Accetta" o "Rifiuta"';
    END IF;
    
    -- Verifica che l'offerta esista e ottieni proprietario dell'annuncio
    SELECT a.FK_Utente INTO proprietario_annuncio
    FROM Offerta of
    JOIN Annuncio a ON of.FK_Annuncio = a.ID_Annuncio
    WHERE of.ID_Offerta = in_id_offerta;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Offerta non trovata';
    END IF;
    
    -- Verifica che l'utente sia il proprietario dell'annuncio

    IF proprietario_annuncio != in_matricola_proprietario THEN
        RAISE EXCEPTION 'Non hai i permessi per accettare/rifiutare questa offerta';
    END IF;
    
    -- Aggiorna lo stato dell'offerta
    UPDATE Offerta 
    SET Stato = nuovo_stato
    WHERE ID_Offerta = in_id_offerta;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


---- SECONDA ----

-- Funzione per aggiornare i dati di un utente
CREATE OR REPLACE FUNCTION fun_aggiornaUtente(
    in_matricola VARCHAR,
    in_nome VARCHAR DEFAULT NULL,
    in_cognome VARCHAR DEFAULT NULL,
    in_username VARCHAR DEFAULT NULL,
    in_password VARCHAR DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE Utente
    SET 
        Nome = in_nome,
        Cognome = in_cognome,
        Username = in_username,
        Password = in_password
    WHERE Matricola = in_matricola;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Utente non trovato';
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;




---- INSERT FUNCTIONS ----


---- PRIMA ----

-- Funzione per inserire un nuovo utente
CREATE OR REPLACE FUNCTION fun_inserisciUtente(
    in_matricola VARCHAR,
    in_nome VARCHAR,
    in_cognome VARCHAR,
    in_username VARCHAR,
    in_password VARCHAR
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO Utente (Matricola, Nome, Cognome, Username, Password)
    VALUES (in_matricola, in_nome, in_cognome, in_username, in_password);
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


---- SECONDA ----

-- Funzione per inserire un nuovo oggetto
CREATE OR REPLACE FUNCTION fun_inserisciOggetto(
    in_id_oggetto VARCHAR,
    in_nome VARCHAR,
    in_condizioni VARCHAR,
    in_dimensione VARCHAR,
    in_peso_kg FLOAT,
    in_matricola_utente VARCHAR
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO Oggetto (ID_Oggetto, Nome, Condizioni, Dimensione, Peso_Kg, FK_Utente)
    VALUES (in_id_oggetto, in_nome, in_condizioni, in_dimensione, in_peso_kg, in_matricola_utente);
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


---- TERZA ----

-- Funzione per inserire un nuovo annuncio  
CREATE OR REPLACE FUNCTION fun_inserisciAnnuncio(
    in_id_annuncio VARCHAR,
    in_titolo VARCHAR,
    in_descrizione VARCHAR,
    in_categoria categoriaAnnuncio,
    in_tipo tipoAnnuncio,
    in_prezzo_vendita FLOAT,
    in_id_oggetto VARCHAR,
    in_matricola_utente VARCHAR
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO Annuncio (ID_Annuncio, Titolo, Descrizione, Categoria, Tipo, prezzoVendita, FK_Oggetto, FK_Utente, DataPubblicazione)
    VALUES (in_id_annuncio, in_titolo, in_descrizione, in_categoria, in_tipo, in_prezzo_vendita, in_id_oggetto, in_matricola_utente, CURRENT_DATE);
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;



---- QUARTA ----

-- Funzione per inserire una nuova offerta

CREATE OR REPLACE FUNCTION fun_inserisciOfferta(
    in_id_offerta VARCHAR,
    in_prezzo_offerta FLOAT,
    in_commento VARCHAR,
    in_tipo tipoAnnuncio,
    in_matricola_utente VARCHAR,
    in_id_annuncio VARCHAR,
    in_oggetto_offerto VARCHAR DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO Offerta (ID_Offerta, PrezzoOfferta, Commento, DataOfferta, Stato, Tipo, FK_Utente, FK_Annuncio, ID_OggettoOfferto)
    VALUES (in_id_offerta, in_prezzo_offerta, in_commento, CURRENT_DATE, 'Attesa', in_tipo, in_matricola_utente, in_id_annuncio, in_oggetto_offerto);
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;



---- QUINTA ----

-- Funzione per inserire una modalità di consegna


CREATE OR REPLACE FUNCTION fun_inserisciConsegna(
    in_id_consegna VARCHAR,
    in_sede_uni VARCHAR,
    in_ora_inizio VARCHAR,
    in_ora_fine VARCHAR,
    in_note VARCHAR,
    in_data DATE,
    in_id_annuncio VARCHAR
)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO ModConsegna (ID_Consegna, sedeUni, oraInizioFasciaOraria, oraFineFasciaOraria, Note, Data, FK_Annuncio)
    VALUES (in_id_consegna, in_sede_uni, in_ora_inizio, in_ora_fine, in_note, in_data, in_id_annuncio);
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;



---- UTILITY FUNCTIONS (solo dove strettamente necessario) ----

-- Funzione per verificare se un oggetto appartiene a un utente (usata nelle DELETE functions)
CREATE OR REPLACE FUNCTION fun_verificaProprietaOggetto(in_id_oggetto VARCHAR, in_matricola VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (SELECT 1 FROM Oggetto WHERE ID_Oggetto = in_id_oggetto AND FK_Utente = in_matricola);
END;
$$ LANGUAGE plpgsql;

-- Funzione per verificare se un username è già in uso

CREATE OR REPLACE FUNCTION fun_verificaUsernameEsistente(in_username VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (SELECT 1 FROM Utente WHERE Username = in_username);
END;
$$ LANGUAGE plpgsql;




---- DELETE FUNCTIONS ----

---- PRIMA ----

-- Funzione per eliminare un utente (elimina anche tutti i suoi dati collegati per CASCADE)
-- Il controllo di esistenza non è necessario: se l'utente non esiste, DELETE non fa nulla

CREATE OR REPLACE FUNCTION fun_eliminaUtente(in_matricola VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    -- Elimina l'utente (CASCADE eliminerà automaticamente oggetti, annunci, offerte)
    DELETE FROM Utente WHERE Matricola = in_matricola;
    

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Utente non trovato: %', in_matricola;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

---- SECONDA ----

-- Funzione per eliminare un oggetto (solo se non ha annunci attivi)

CREATE OR REPLACE FUNCTION fun_eliminaOggetto(in_id_oggetto VARCHAR, in_matricola_proprietario VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    -- Verifica che non ci siano annunci attivi per questo oggetto
    IF EXISTS (SELECT 1 FROM Annuncio WHERE FK_Oggetto = in_id_oggetto AND Stato = 'Attivo') THEN
        RAISE EXCEPTION 'Impossibile eliminare l''oggetto: ha annunci attivi';
    END IF;
    
    -- Elimina l'oggetto (solo se appartiene all'utente)
    DELETE FROM Oggetto WHERE ID_Oggetto = in_id_oggetto AND FK_Utente = in_matricola_proprietario;
    
    -- Verifica se è stato eliminato qualcosa
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Oggetto non trovato o non di tua proprietà: %', in_id_oggetto;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;



---- TERZA ----

-- Funzione per eliminare un annuncio (solo se è del proprietario e non ha offerte accettate)
CREATE OR REPLACE FUNCTION fun_eliminaAnnuncio(in_id_annuncio VARCHAR, in_matricola_proprietario VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN

    -- Verifica che non ci siano offerte accettate
    IF EXISTS (SELECT 1 FROM Offerta WHERE FK_Annuncio = in_id_annuncio AND Stato = 'Accettata') THEN
        RAISE EXCEPTION 'Impossibile eliminare l''annuncio: ha offerte accettate';
    END IF;
    
    -- Elimina l'annuncio (solo se appartiene all'utente) (CASCADE eliminerà le offerte collegate)
    DELETE FROM Annuncio WHERE ID_Annuncio = in_id_annuncio AND FK_Utente = in_matricola_proprietario;
    
    -- Verifica se è stato eliminato qualcosa
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Annuncio non trovato o non hai i permessi per eliminarlo';
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

---- QUARTA ----

-- Funzione per eliminare un'offerta (solo se è propria e non è accettata)
CREATE OR REPLACE FUNCTION fun_eliminaOfferta(in_id_offerta VARCHAR, in_matricola_offerente VARCHAR)
RETURNS BOOLEAN AS $$
DECLARE
    stato_offerta statoOfferta;
BEGIN
    -- Verifica che l'offerta esista, appartenga all'utente e ottieni lo stato
    SELECT Stato INTO stato_offerta 
    FROM Offerta 
    WHERE ID_Offerta = in_id_offerta AND FK_Utente = in_matricola_offerente;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Offerta non trovata o non di tua proprietà: %', in_id_offerta;
    END IF;
    
    -- Verifica che l'offerta non sia già accettata
    IF stato_offerta = 'Accettata' THEN
        RAISE EXCEPTION 'Impossibile eliminare un''offerta accettata';
    END IF;
    
    -- Elimina l'offerta
    DELETE FROM Offerta WHERE ID_Offerta = in_id_offerta;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

---- QUINTA ----

-- Funzione per eliminare una modalità di consegna
CREATE OR REPLACE FUNCTION fun_eliminaConsegna(in_id_consegna VARCHAR, in_matricola_proprietario VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    -- Elimina la modalità di consegna (solo se l'utente è proprietario dell'annuncio collegato)
    DELETE FROM ModConsegna 
    WHERE ID_Consegna = in_id_consegna 
    AND FK_Annuncio IN (SELECT ID_Annuncio FROM Annuncio WHERE FK_Utente = in_matricola_proprietario);
    
    -- Verifica se è stato eliminato qualcosa
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Modalità di consegna non trovata o non hai i permessi';
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

---- SESTA ----

-- Funzione per eliminare tutte le offerte rifiutate di un utente (pulizia)
CREATE OR REPLACE FUNCTION fun_pulisciOfferteRifiutate(in_matricola VARCHAR)
RETURNS INTEGER AS $$
DECLARE
    offerte_eliminate INTEGER;
BEGIN
    -- Elimina tutte le offerte rifiutate dell'utente
    DELETE FROM Offerta 
    WHERE FK_Utente = in_matricola AND Stato = 'Rifiutata';
    
    GET DIAGNOSTICS offerte_eliminate = ROW_COUNT;
    
    RETURN offerte_eliminate;
END;
$$ LANGUAGE plpgsql;

