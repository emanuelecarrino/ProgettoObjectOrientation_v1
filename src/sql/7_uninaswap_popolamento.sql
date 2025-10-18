

-- POPOLAMENTO DATABASE UNINASWAP

-- Pulizia preventiva delle tabelle (in ordine inverso per rispettare i vincoli)
DELETE FROM ModConsegna;
DELETE FROM Offerta;
DELETE FROM Annuncio;
DELETE FROM Oggetto;
DELETE FROM Utente;

-- Pulizia (ord. inverso per vincoli FK)
TRUNCATE TABLE ModConsegna, Offerta, Annuncio, Oggetto, Utente RESTART IDENTITY CASCADE;

-- UTENTI (pochi per test)
INSERT INTO Utente (Matricola, Nome, Cognome, Email, Username, Password, DataNascita, Genere) VALUES
('N86000001','Luca','Verdi','luca.verdi@example.com','lucav','pwd123','2000-02-15','M'),
('N86000002','Anna','Rossi','anna.rossi@example.com','annar','pwd456','1999-06-10','F'),
('N86000003','Marco','Bianchi','marco.bianchi@example.com','marcob','pwd789','2001-11-03','M'),
('N86000004','Giulia','Neri','giulia.neri@example.com','giulian','pwd321','1998-01-25','F'),
('N86000005','Paolo','Russo','paolo.russo@example.com','paolor','pwd654','2000-07-30','M'),
('N86000006','Sara','Ferrari','sara.ferrari@example.com','saraf','pwd987','1997-12-12','F');

-- OGGETTI
INSERT INTO Oggetto (ID_Oggetto, Nome, numProprietari, Condizioni, Dimensione, Peso_Kg, FK_Utente) VALUES
('OBJ-00001','Libro Analisi I',1,'Buono','30x20cm',0.50,'N86000001'),
('OBJ-00002','Laptop HP',1,'Ottimo','35x25cm',2.10,'N86000002'),
('OBJ-00003','Giacca Invernale',1,'Buono','M',0.80,'N86000003'),
('OBJ-00004','Zaino Invicta',1,'Ottimo','40x30cm',0.70,'N86000004'),
('OBJ-00005','Libro Fisica',1,'Accettabile','28x19cm',0.50,'N86000005'),
('OBJ-00006','Tablet Samsung',1,'Buono','25x17cm',0.45,'N86000006');

-- oggetto extra libero per offerte (non collegato ad annunci)
INSERT INTO Oggetto (ID_Oggetto, Nome, numProprietari, Condizioni, Dimensione, Peso_Kg, FK_Utente) VALUES
('OBJ-00007','Cuffie Bluetooth',1,'Nuovo','10x10cm',0.20,'N86000002');

-- Oggetto extra per N86000001 da usare in offerte di scambio (non collegato ad annunci)
INSERT INTO Oggetto (ID_Oggetto, Nome, numProprietari, Condizioni, Dimensione, Peso_Kg, FK_Utente) VALUES
('OBJ-00008','Mouse Logitech',1,'Ottimo','12x7cm',0.10,'N86000001');

-- ANNUNCI
-- ANNUNCI (ID formattati come ANN-xxxxx)
INSERT INTO Annuncio (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato, FK_Utente, FK_Oggetto, Tipo, prezzoVendita) VALUES
('ANN-00001','Vendo libro Analisi I','Libro universitario in buone condizioni', CURRENT_DATE, 'LibriTesto','Attivo','N86000001','OBJ-00001','Vendita', 18.00),
('ANN-00002','Scambio laptop HP','Laptop quasi nuovo, cerco tablet', CURRENT_DATE, 'Informatica','Attivo','N86000002','OBJ-00002','Scambio', NULL),
('ANN-00003','Regalo giacca invernale','Taglia M, buona condizione', CURRENT_DATE, 'Abbigliamento','Attivo','N86000003','OBJ-00003','Regalo', NULL),
('ANN-00004','Vendo zaino Invicta','Usato poco', CURRENT_DATE, 'Altro','Attivo','N86000004','OBJ-00004','Vendita', 25.00),
('ANN-00005','Vendo libro di Fisica','Alcune sottolineature', CURRENT_DATE, 'LibriTesto','Attivo','N86000005','OBJ-00005','Vendita', 12.50),
('ANN-00006','Scambio tablet Samsung','Cerco laptop o phone', CURRENT_DATE, 'Informatica','Attivo','N86000006','OBJ-00006','Scambio', NULL);

-- OFFERTE (uso colonne esplicite per evitare ambiguità)
-- OFFERTE (ID formattati come OFF-xxxxx) - aggiorno i riferimenti a ANN-xxxxx
INSERT INTO Offerta (ID_Offerta, PrezzoOfferta, Commento, DataOfferta, Stato, Tipo, FK_Utente, FK_Annuncio, ID_OggettoOfferto) VALUES
('OFF-00001', 18.00, 'Prendo subito',           CURRENT_DATE, 'Accettata', 'Vendita', 'N86000005', 'ANN-00001', NULL),
('OFF-00002', 17.00, 'Posso offrire 17',       CURRENT_DATE, 'Rifiutata', 'Vendita', 'N86000006', 'ANN-00001', NULL),
('OFF-00003', NULL,  'Posso dare oggetto in scambio', CURRENT_DATE, 'Attesa',    'Scambio',  'N86000002', 'ANN-00006', 'OBJ-00007'),
('OFF-00004', 24.00, 'Interessato, ci sto',    CURRENT_DATE, 'Rifiutata', 'Vendita',  'N86000001', 'ANN-00004', NULL);

-- Offerte aggiuntive per test REPORT (utente N86000001 come offerente)
-- Obiettivo: avere totali per tipologia, accettate per tipologia e statistiche su Vendita accettate (min/avg/max)
INSERT INTO Offerta (ID_Offerta, PrezzoOfferta, Commento, DataOfferta, Stato, Tipo, FK_Utente, FK_Annuncio, ID_OggettoOfferto) VALUES
('OFF-00005', 23.00, 'Offerta alternativa',          CURRENT_DATE, 'Accettata', 'Vendita', 'N86000001', 'ANN-00004', NULL),
('OFF-00006', 11.00, 'Troppo alto, propongo 11',     CURRENT_DATE, 'Rifiutata', 'Vendita', 'N86000001', 'ANN-00005', NULL),
('OFF-00007', 12.50, 'Ok per il prezzo pieno',       CURRENT_DATE, 'Accettata', 'Vendita', 'N86000001', 'ANN-00005', NULL),
('OFF-00008', NULL,  'Propongo scambio con mio oggetto', CURRENT_DATE, 'Accettata', 'Scambio', 'N86000001', 'ANN-00006', 'OBJ-00008'),
('OFF-00009', NULL,  'Mi propongo per il ritiro',     CURRENT_DATE, 'Accettata', 'Regalo',  'N86000001', 'ANN-00003', NULL);
