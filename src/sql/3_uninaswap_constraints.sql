----- FOREIGN KEY -----


-- FK di Oggetto.ID_Oggetto ad Annuncio

ALTER TABLE Annuncio
ADD CONSTRAINT FK_oggettoToAnnuncio
FOREIGN KEY (FK_Oggetto) REFERENCES Oggetto(Id_Oggetto) ON DELETE CASCADE;

-- FK di Utente.Matricola ad Annuncio
ALTER TABLE Annuncio
ADD CONSTRAINT FK_utenteToAnnuncio
FOREIGN KEY (FK_Utente) REFERENCES Utente(Matricola) ON DELETE CASCADE;

-- FK di Utente.Matricola ad Oggetto
ALTER TABLE Oggetto
ADD CONSTRAINT FK_utenteToOggetto
FOREIGN KEY (FK_Utente) REFERENCES Utente(Matricola) ON DELETE CASCADE;

-- FK di Annuncio.ID_Annuncio ad Offerta
ALTER TABLE Offerta
ADD CONSTRAINT FK_annuncioToOfferta
FOREIGN KEY (FK_Annuncio) REFERENCES Annuncio(ID_Annuncio) ON DELETE CASCADE;

-- FK di Utente.Matricola ad Offerta
ALTER TABLE Offerta
ADD CONSTRAINT FK_utenteToOfferta
FOREIGN KEY (FK_Utente) REFERENCES Utente(Matricola) ON DELETE CASCADE;

-- FK di Oggetto.ID_Oggetto ad Offerta (per l'oggetto offerto)
ALTER TABLE Offerta
ADD CONSTRAINT FK_oggettoToOfferta
FOREIGN KEY (ID_OggettoOfferto) REFERENCES Oggetto(ID_Oggetto) ON DELETE CASCADE;

-- FK di Annuncio.ID_Annuncio a ModConsegna
ALTER TABLE ModConsegna
ADD CONSTRAINT FK_annuncioToModConsegna
FOREIGN KEY (FK_Annuncio) REFERENCES Annuncio(ID_Annuncio) ON DELETE CASCADE;




----- CHECK -----

-- Questo controlla che se l'annuncio è di tipo vendita allora prezzoVendita non deve essere NULL e deve essere > 0,
-- nel caso in cui non fosse di tipo vendita, il prezzoVendita deve essere NULLABLE

-- (Questo check contiene sia checkPrezzoVenditaNonNegativo che checkPrezzoVenditaNonObbligatorio)

ALTER TABLE Annuncio
ADD CONSTRAINT checkPrezzoVenditaCorretto
CHECK (
  (Tipo = 'VENDITA' AND prezzoVendita IS NOT NULL AND prezzoVendita > 0)
  OR
  (Tipo <> 'VENDITA' AND prezzoVendita IS NULL)
);


-- Controlla che il peso dell'oggetto sia positivo

ALTER TABLE Oggetto
ADD CONSTRAINT checkControlloPeso
CHECK (Peso_Kg > 0);

-- Controlla che il numero di proprietari sia positivo

ALTER TABLE Oggetto
ADD CONSTRAINT checkNumProprietariPositivo
CHECK (numProprietari > 0);

-- Controlla che la password abbia almeno 6 caratteri

ALTER TABLE Utente
ADD CONSTRAINT checkPassword
CHECK (char_length(password) > 5);


-- Controlla che la matricola inizi con N86 e abbia 9 caratteri totali

ALTER TABLE Utente
ADD CONSTRAINT checkMatricolaFormato
CHECK (Matricola ~ '^N86[0-9]{6}$');

-- Email univoca
ALTER TABLE Utente
ADD CONSTRAINT uk_emailUtente UNIQUE (Email);

-- Formato email basilare (case-insensitive)
--ALTER TABLE Utente
--ADD CONSTRAINT checkFormatoEmail
--CHECK (Email ~* '^[A-Za-z0-9]+@[A-Za-z0-9]+\.[A-Za-z]{2,}$');

-- DataNascita non futura
ALTER TABLE Utente
ADD CONSTRAINT checkDataNascitaNonFutura
CHECK (DataNascita IS NULL OR DataNascita <= CURRENT_DATE);

-- Genere (valori ammessi opzionali) se non nullo
ALTER TABLE Utente
ADD CONSTRAINT checkGenereValido
CHECK (Genere IS NULL OR Genere IN ('M','F','ALTRO'));