SET search_path TO uninaswap, public;


-- Creazione dei vari enum

CREATE TYPE statoOfferta AS ENUM ('Attesa', 'Accettata', 'Rifiutata');
CREATE TYPE statoAnnuncio AS ENUM ('Attivo', 'Venduto', 'Scambiato', 'Regalato');
CREATE TYPE categoriaAnnuncio AS ENUM ('Libritesto', 'Informatica', 'Abbigliamento', 'Altro');
CREATE TYPE tipoAnnuncio AS ENUM ('Vendita', 'Scambio', 'Regalo');
CREATE TYPE tipoOfferta AS ENUM ('Vendita', 'Scambio', 'Regalo');

-- Creazione della tabella Utente (allineata a UtenteDTO: nome, cognome, email, matricola, username, password, dataNascita, genere)

CREATE TABLE Utente (
    Matricola      VARCHAR(9)  PRIMARY KEY,
    Nome           VARCHAR(30) NOT NULL,
    Cognome        VARCHAR(30) NOT NULL,
    Email          VARCHAR(120) NOT NULL UNIQUE,
    Username       VARCHAR(15) NOT NULL UNIQUE,
    Password       VARCHAR(60) NOT NULL,
    DataNascita    DATE,
    Genere         VARCHAR(15)
);


-- Creazione della tabella Oggetto (allineata a OggettoDTO)

CREATE TABLE Oggetto (

    ID_Oggetto     VARCHAR(10) PRIMARY KEY,
    Nome           VARCHAR(30) NOT NULL,
    numProprietari INTEGER NOT NULL DEFAULT 1,
    Condizioni     VARCHAR(80) NOT NULL,
    Dimensione     VARCHAR(10),
    Peso_Kg        FLOAT,
    FK_Utente      VARCHAR NOT NULL
);


-- Creazione della tabella Annuncio (ordine secondo AnnuncioDTO)

CREATE TABLE Annuncio (
    ID_Annuncio       VARCHAR(10) PRIMARY KEY,
    Titolo            VARCHAR(50) NOT NULL,
    Descrizione       VARCHAR(200),
    Stato             statoAnnuncio NOT NULL DEFAULT 'Attivo',
    Categoria         categoriaAnnuncio NOT NULL,
    DataPubblicazione DATE NOT NULL DEFAULT CURRENT_DATE,
    FK_Utente         VARCHAR NOT NULL,        
    FK_Oggetto        VARCHAR NOT NULL,
    Tipo              tipoAnnuncio NOT NULL,
    prezzoVendita     FLOAT
);


-- Creazione della tabella Offerta (allineata a OffertaDTO - ID_OggettoOfferto opzionale per scambi)

CREATE TABLE Offerta (

    ID_Offerta        VARCHAR(10) PRIMARY KEY,
    FK_Annuncio       VARCHAR NOT NULL,
    FK_Utente         VARCHAR NOT NULL,
    DataOfferta       DATE NOT NULL,
    Stato             statoOfferta NOT NULL DEFAULT 'Attesa',
    PrezzoOfferta     FLOAT,
    Commento          VARCHAR(150),
    Tipo              tipoOfferta NOT NULL,
    ID_OggettoOfferto VARCHAR
);


-- Creazione della tabella ModConsegna (allineata a ModConsegnaDTO)

CREATE TABLE ModConsegna (

    ID_Consegna  VARCHAR(10) PRIMARY KEY,
    FK_Annuncio  VARCHAR NOT NULL UNIQUE,
    sedeUni      VARCHAR(30) NOT NULL,
    Data         DATE NOT NULL,
    oraInizioFasciaOraria VARCHAR(5) NOT NULL,
    oraFineFasciaOraria  VARCHAR(5) NOT NULL,
    Note         VARCHAR(150),
    fasciaOraria VARCHAR(30)  -- campo derivato eventuale (DTO lo contiene concatenato), opzionale per futuri usi
);



