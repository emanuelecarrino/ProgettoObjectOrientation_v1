---- PRIMA ----

-- Questa funzione controlla il prezzoOfferta che ha valore massimo (< prezzoVendita) e non negativa in caso di AnnuncioVendita
-- inoltre viene controllato che l'annuncio sia di stato diverso da "Venduto", "Scambiato" o "Regalato" per poter inserire
-- un'offerta

CREATE OR REPLACE FUNCTION fun_controlloOfferta()
RETURNS TRIGGER AS
$$
DECLARE
    annuncioOfferto Annuncio%ROWTYPE;
BEGIN
    SELECT * INTO annuncioOfferto
    FROM Annuncio
    WHERE ID_Annuncio = NEW.FK_Annuncio;
    IF annuncioOfferto.Stato = 'Venduto' OR annuncioOfferto.Stato = 'Scambiato' OR annuncioOfferto.Stato = 'Regalato' THEN
        RAISE EXCEPTION 'L''annuncio non è più disponibile: impossibile fare un''offerta';
    END IF;
    IF annuncioOfferto.Tipo = 'Vendita' AND NEW.prezzoOfferta IS NULL THEN
        RAISE EXCEPTION 'Deve esserci un''offerta.';
    END IF;

    IF annuncioOfferto.Tipo = 'Vendita' AND (NEW.prezzoOfferta > annuncioOfferto.prezzoVendita OR NEW.prezzoOfferta < 0) THEN
        RAISE EXCEPTION 'L''offerta non è valida';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- SECONDA ----

-- Questa funzione garantisce che ogni oggetto inserito all'interno di un annuncio non sia già in utilizzo in un altro
-- annuncio e un'altra offerta in attesa (di scambio)

CREATE OR REPLACE FUNCTION fun_oggettoUnivocoPerAnnuncio()
RETURNS TRIGGER AS $$
DECLARE
    r RECORD;
    s RECORD;
BEGIN
    FOR r IN SELECT * FROM Annuncio WHERE Stato = 'Attivo' LOOP
        IF (r.ID_Annuncio <> NEW.ID_Annuncio AND r.FK_Oggetto = NEW.FK_Oggetto) THEN
            RAISE EXCEPTION 'È già presente un annuncio attivo con l''oggetto inserito.';
        END IF;
    END LOOP;

    FOR s IN SELECT * FROM Offerta WHERE Stato = 'Attesa' LOOP
        IF (s.ID_OggettoOfferto = NEW.FK_Oggetto) THEN
            RAISE EXCEPTION 'Quest''oggetto è già in utilizzo per un''offerta.';
        END IF;
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- TERZA ----

-- Questa funzione controlla che un oggetto offerto in uno scambio non sia già utilizzato
-- in altri annunci attivi o altre offerte in attesa

CREATE OR REPLACE FUNCTION fun_oggettoUnivocoPerScambio()
RETURNS TRIGGER AS $$
DECLARE
    r RECORD; 
    s RECORD;
BEGIN
    FOR r IN SELECT * FROM Annuncio WHERE Stato = 'Attivo' LOOP
        IF (r.FK_Oggetto = NEW.ID_OggettoOfferto) THEN
            RAISE EXCEPTION 'È già presente un annuncio attivo con l''oggetto inserito.';
        END IF;
    END LOOP;

    FOR s IN SELECT * FROM Offerta WHERE Stato = 'Attesa' LOOP
        IF (s.ID_OggettoOfferto = NEW.ID_OggettoOfferto AND s.ID_Offerta <> NEW.ID_Offerta) THEN
            RAISE EXCEPTION 'Quest''oggetto è già in utilizzo per un''offerta.';
        END IF;
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- QUARTA ----

-- Questa funzione regola che la data di consegna non sia precedente a quella attuale 

CREATE OR REPLACE FUNCTION fun_regolaDataConsegna()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Data < CURRENT_DATE THEN
        RAISE EXCEPTION 'Bisogna inserire una data valida';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- QUINTA ----


-- Questa funzione controlla che per gli annunci di tipo "Regalo" sia obbligatorio inserire un commento

CREATE OR REPLACE FUNCTION fun_messaggioSuOfferta()
RETURNS TRIGGER AS $$
DECLARE
    tipo_A tipoAnnuncio;
BEGIN

    SELECT Tipo INTO tipo_A
    FROM Annuncio
    WHERE ID_Annuncio = NEW.FK_Annuncio;

    IF tipo_A = 'Regalo' AND NEW.Commento IS NULL THEN
        RAISE EXCEPTION 'Il commento non deve essere NULL.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- SESTA ----

-- Questa funzione incrementa il numero di proprietari e cambia il proprietario quando un'offerta viene accettata

CREATE OR REPLACE FUNCTION fun_incrementoProprietari()
RETURNS TRIGGER AS $$
DECLARE
    annuncioOfferto Annuncio%ROWTYPE;
BEGIN

    -- Scatta solo se lo stato è appena diventato 'Accettata'

    IF (NEW.Stato = 'Accettata') THEN
        SELECT * INTO annuncioOfferto
        FROM Annuncio
        WHERE ID_Annuncio = NEW.FK_Annuncio;
        UPDATE Oggetto
        SET numProprietari = numProprietari + 1, FK_Utente = NEW.FK_Utente
        WHERE ID_Oggetto = annuncioOfferto.FK_Oggetto;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- SETTIMA ----

-- Questa funzione controlla che la data di pubblicazione dell'annuncio sia quella
-- corrente in caso di inserimento

CREATE OR REPLACE FUNCTION fun_controlloDataAnnuncio()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.DataPubblicazione <> CURRENT_DATE) THEN
        RAISE EXCEPTION 'La data è errata.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- OTTAVA ----

--- Questa funzione controlla la presenza di un oggetto in un'offerta, per scambi deve esserci, per vendite/regali no, 
--- e verifica anche che l'oggetto appartenga effettivamente all'utente che fa l'offerta

CREATE OR REPLACE FUNCTION fun_controlloOggettoOfferta()
RETURNS TRIGGER AS $$
DECLARE
    annuncioCorrente Annuncio%ROWTYPE;
    proprietarioOggetto VARCHAR;
BEGIN

    -- Controllo che l'offerta, se di tipo scambio, abbia un oggetto proposto

    SELECT * INTO annuncioCorrente
    FROM Annuncio
    WHERE ID_Annuncio = NEW.FK_Annuncio;

    IF (annuncioCorrente.Tipo = 'Scambio' AND NEW.ID_OggettoOfferto IS NULL) THEN
        RAISE EXCEPTION 'L''offerta scambio necessita di un oggetto da scambiare.';
    ELSIF (annuncioCorrente.Tipo <> 'Scambio' AND NEW.ID_OggettoOfferto IS NOT NULL) THEN
        RAISE EXCEPTION 'Un''annuncio Vendita/Regalo non richiede un oggetto in offerta.';
    END IF;

    -- Controllo che l'oggetto appartenga effettivamente al proprietario

    IF (annuncioCorrente.Tipo = 'Scambio' AND NEW.ID_OggettoOfferto IS NOT NULL) THEN
        SELECT FK_Utente INTO proprietarioOggetto
        FROM Oggetto
    WHERE ID_Oggetto = NEW.ID_OggettoOfferto;
        
    IF (proprietarioOggetto <> NEW.FK_Utente) THEN
            RAISE EXCEPTION 'Non puoi offrire un oggetto che non possiedi.';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- NONA ----

-- Questa funzione gestisce che in caso di offerta accettata, le altre dello stesso annuncio vengano rifiutate  
-- automaticamente e si aggiorni lo stato dell'annuncio di conseguenza (se non già aggiornato)

CREATE OR REPLACE FUNCTION fun_controlloVendite()
RETURNS TRIGGER AS $$
DECLARE
    annuncio Annuncio%ROWTYPE;
BEGIN
    SELECT * INTO annuncio 
    FROM Annuncio 
    WHERE ID_Annuncio = NEW.FK_Annuncio;

    -- In caso di offerta accettata, rifiuta le altre relative allo stesso annuncio

    IF NEW.Stato = 'Accettata' THEN
    UPDATE Offerta SET Stato = 'Rifiutata'
    WHERE FK_Annuncio = NEW.FK_Annuncio AND ID_Offerta <> NEW.ID_Offerta AND Stato <> 'Rifiutata';
    END IF;

    -- Aggiorna lo stato dell'annuncio nel caso in cui l'offerta implementata o aggiornata sia accettata

    IF NEW.Stato = 'Accettata' THEN
        IF annuncio.Tipo = 'Vendita' THEN
            UPDATE Annuncio SET Stato = 'Venduto' WHERE ID_Annuncio = NEW.FK_Annuncio;
        ELSIF annuncio.Tipo = 'Scambio' THEN
            UPDATE Annuncio SET Stato = 'Scambiato' WHERE ID_Annuncio = NEW.FK_Annuncio;
        ELSIF annuncio.Tipo = 'Regalo' THEN
            UPDATE Annuncio SET Stato = 'Regalato' WHERE ID_Annuncio = NEW.FK_Annuncio;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- DECIMA ----

-- Questa funzione controlla che un annuncio possa essere marcato come "Venduto","Scambiato" o "Regalato" 
-- solo se ha almeno un'offerta accettata

CREATE OR REPLACE FUNCTION fun_controlloOffertaAccettata()
RETURNS TRIGGER AS $$
DECLARE
    n_offerte_accettate INTEGER;
BEGIN
    IF NEW.Stato IN ('Venduto', 'Scambiato', 'Regalato') THEN
        SELECT COUNT(*) INTO n_offerte_accettate FROM Offerta WHERE FK_Annuncio = NEW.ID_Annuncio AND Stato = 'Accettata';
        IF n_offerte_accettate = 0 THEN
            RAISE EXCEPTION 'Non è possibile impostare lo stato "%" senza almeno un''offerta accettata.', NEW.Stato;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- UNDICESIMA ----

-- Questa funzione permette di inserire una modalità di consegna solo per annunci non più attivi

CREATE OR REPLACE FUNCTION fun_controlloModConsegna()
RETURNS TRIGGER AS $$
DECLARE
    stato_annuncio statoAnnuncio;
BEGIN
    SELECT Stato INTO stato_annuncio FROM Annuncio WHERE ID_Annuncio = NEW.FK_Annuncio;
    IF stato_annuncio NOT IN ('Venduto', 'Scambiato', 'Regalato') THEN
        RAISE EXCEPTION 'Non è possibile inserire una consegna per un annuncio attivo';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- DODICESIMA ----

-- Questa funzione impedisce all'autore di un annuncio di fare offerte sul proprio annuncio

CREATE OR REPLACE FUNCTION fun_offertaStessoVenditore()
RETURNS TRIGGER AS $$
DECLARE
    annuncioOfferto Annuncio%ROWTYPE;
BEGIN
    SELECT * INTO annuncioOfferto
    FROM Annuncio
    WHERE ID_Annuncio = NEW.FK_Annuncio;

    IF (NEW.FK_Utente = annuncioOfferto.FK_Utente) THEN
        RAISE EXCEPTION 'L''offerta non può essere eseguita da chi ha creato l''annuncio.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- TREDICESIMA ----

-- Questa funzione che impedisce a un utente di fare più offerte sullo stesso annuncio se ne ha già una in attesa

CREATE OR REPLACE FUNCTION fun_offertaRipetuta()
RETURNS TRIGGER AS $$
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (SELECT * FROM Offerta) LOOP 
    IF r.FK_Utente = NEW.FK_Utente AND r.FK_Annuncio = NEW.FK_Annuncio AND r.Stato = 'Attesa' THEN
            RAISE EXCEPTION 'Non si può effettuare un''offerta se ne è presente già una in attesa.';
        END IF;
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---- QUATTORDICESIMA ----

-- Verifica che l'oggetto associato all'annuncio appartenga all'utente creatore.

CREATE OR REPLACE FUNCTION fun_proprietaOggettoAnnuncio()
RETURNS TRIGGER AS $$
DECLARE
    owner VARCHAR;
BEGIN
    SELECT FK_Utente INTO owner
    FROM Oggetto
    WHERE ID_Oggetto = NEW.FK_Oggetto;

    IF owner IS NULL THEN
        RAISE EXCEPTION 'Oggetto inesistente';
    END IF;

    -- Blocco cambio oggetto in UPDATE

    IF TG_OP = 'UPDATE' AND NEW.FK_Oggetto <> OLD.FK_Oggetto THEN
        RAISE EXCEPTION 'Cambio oggetto non consentito.';
    END IF;

    IF owner <> NEW.FK_Utente THEN
        RAISE EXCEPTION 'L''oggetto non appartiene all''utente.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;