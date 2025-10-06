-- POPOLAMENTO DATABASE UNINASWAP


-- Pulizia preventiva delle tabelle (in ordine inverso per rispettare i vincoli)

DELETE FROM ModConsegna;
DELETE FROM Offerta;
DELETE FROM Annuncio;
DELETE FROM Oggetto;
DELETE FROM Utente;


-- UTENTE
INSERT INTO Utente (Matricola, Nome, Cognome, Password, Username) VALUES
('N86000001', 'Luca', 'Verdi', 'pwd123', 'lucav'),
('N86000002', 'Anna', 'Rossi', 'pwd456', 'annar'),
('N86000003', 'Marco', 'Bianchi', 'pwd789', 'marcob'),
('N86000004', 'Giulia', 'Neri', 'pwd321', 'giulian'),
('N86000005', 'Paolo', 'Russo', 'pwd654', 'paolor'),
('N86000006', 'Sara', 'Ferrari', 'pwd987', 'saraf'),
('N86000007', 'Davide', 'Esposito', 'pwd111', 'davidee'),
('N86000008', 'Elena', 'Greco', 'pwd222', 'elenag'),
('N86000009', 'Francesco', 'Martini', 'pwd333', 'francescom'),
('N86000010', 'Marta', 'Costa', 'pwd444', 'martac'),
('N86000011', 'Alessio', 'Barbieri', 'pwd555', 'alessiob'),
('N86000012', 'Chiara', 'Moretti', 'pwd666', 'chiaram'),
('N86000013', 'Simone', 'Gallo', 'pwd777', 'simoneg'),
('N86000014', 'Federica', 'Conti', 'pwd888', 'federicac'),
('N86000015', 'Valentina', 'De Luca', 'pwd999', 'valentinadl'),
('N86000016', 'Roberto', 'Bruno', 'pwd000', 'robertob'),
('N86000017', 'Martina', 'Vitale', 'pwd101', 'martinav'),
('N86000018', 'Giovanni', 'Romano', 'pwd202', 'giovanr'),
('N86000019', 'Francesca', 'Marini', 'pwd303', 'francescam'),
('N86000020', 'Andrea', 'Ricci', 'pwd404', 'andrear');

-- OGGETTO
INSERT INTO Oggetto (ID_Oggetto, Nome, numProprietari, Condizioni, Dimensione, Peso_Kg, FK_Utente) VALUES
('OBJ001', 'Libro Analisi I', 1, 'Buono', '30x20cm', 0.50, 'N86000001'),
('OBJ002', 'Laptop HP', 1, 'Ottimo', '35x25cm', 2.10, 'N86000002'),
('OBJ003', 'Giacca Invernale', 1, 'Buono', 'M', 0.80, 'N86000003'),
('OBJ004', 'Zaino Invicta', 1, 'Ottimo', '40x30cm', 0.70, 'N86000004'),
('OBJ005', 'Libro Fisica', 1, 'Accettabile', '28x19cm', 0.50, 'N86000005'),
('OBJ006', 'Tablet Samsung', 1, 'Buono', '25x17cm', 0.45, 'N86000006'),
('OBJ007', 'Scarpe Running', 1, 'Buono', '42', 0.60, 'N86000007'),
('OBJ008', 'Calcolatrice Scientifica', 1, 'Ottimo', '15x8cm', 0.25, 'N86000008'),
('OBJ009', 'Libro Inglese', 1, 'Buono', '27x18cm', 0.40, 'N86000009'),
('OBJ010', 'Mouse Logitech', 1, 'Ottimo', '12x7cm', 0.20, 'N86000010'),
('OBJ011', 'Felpa Adidas', 1, 'Buono', 'L', 0.55, 'N86000011'),
('OBJ012', 'Libro Storia', 1, 'Buono', '29x21cm', 0.48, 'N86000012'),
('OBJ013', 'Auricolari Bluetooth', 1, 'Ottimo', '10x5cm', 0.10, 'N86000013'),
('OBJ014', 'Libro Matematica', 1, 'Buono', '30x20cm', 0.52, 'N86000014'),
('OBJ015', 'Borsa Tracolla', 1, 'Buono', '35x25cm', 0.65, 'N86000015'),
('OBJ016', 'Smartphone Samsung', 1, 'Ottimo', '15x7cm', 0.20, 'N86000016'),
('OBJ017', 'Libro Chimica', 1, 'Buono', '28x20cm', 0.55, 'N86000017'),
('OBJ018', 'Tastiera Meccanica', 1, 'Ottimo', '45x15cm', 1.20, 'N86000018'),
('OBJ019', 'Bicicletta Pieghevole', 1, 'Buono', '60x40cm', 12.50, 'N86000019'),
('OBJ020', 'Chitarra Acustica', 1, 'Ottimo', '100x35cm', 2.80, 'N86000020');

-- ANNUNCIO 

INSERT INTO Annuncio (ID_Annuncio, Titolo, Descrizione, DataPubblicazione, Categoria, Stato, prezzoVendita, Tipo, FK_Oggetto, FK_Utente) VALUES
('ANN001', 'Vendo libro di Analisi', 'Libro universitario in buone condizioni', CURRENT_DATE, 'LibriTesto', 'Attivo', 20.00, 'Vendita', 'OBJ001', 'N86000001'),
('ANN002', 'Scambio laptop HP', 'Laptop HP quasi nuovo, cerco tablet', CURRENT_DATE, 'Informatica', 'Attivo', NULL, 'Scambio', 'OBJ002', 'N86000002'),
('ANN003', 'Regalo giacca', 'Giacca invernale, taglia M', CURRENT_DATE, 'Abbigliamento', 'Attivo', NULL, 'Regalo', 'OBJ003', 'N86000003'),
('ANN004', 'Vendo zaino Invicta', 'Zaino usato poco', CURRENT_DATE, 'Altro', 'Attivo', 25.00, 'Vendita', 'OBJ004', 'N86000004'),
('ANN005', 'Vendo libro di Fisica', 'Libro universitario, alcune sottolineature', CURRENT_DATE, 'LibriTesto', 'Attivo', 15.00, 'Vendita', 'OBJ005', 'N86000005'),
('ANN006', 'Scambio tablet Samsung', 'Tablet in buone condizioni, cerco laptop', CURRENT_DATE, 'Informatica', 'Attivo', NULL, 'Scambio', 'OBJ006', 'N86000006'),
('ANN007', 'Vendo scarpe running', 'Scarpe usate una stagione', CURRENT_DATE, 'Abbigliamento', 'Attivo', 30.00, 'Vendita', 'OBJ007', 'N86000007'),
('ANN008', 'Regalo calcolatrice', 'Calcolatrice scientifica perfetta', CURRENT_DATE, 'Informatica', 'Attivo', NULL, 'Regalo', 'OBJ008', 'N86000008'),
('ANN009', 'Vendo libro Inglese', 'Libro per esame universitario', CURRENT_DATE, 'LibriTesto', 'Attivo', 18.00, 'Vendita', 'OBJ009', 'N86000009'),
('ANN010', 'Scambio mouse Logitech', 'Cerco auricolari bluetooth', CURRENT_DATE, 'Informatica', 'Attivo', NULL, 'Scambio', 'OBJ010', 'N86000010'),
('ANN011', 'Vendo felpa Adidas', 'Felpa taglia L, colore blu', CURRENT_DATE, 'Abbigliamento', 'Attivo', 22.00, 'Vendita', 'OBJ011', 'N86000011'),
('ANN012', 'Regalo libro Storia', 'Libro liceo, ottime condizioni', CURRENT_DATE, 'LibriTesto', 'Attivo', NULL, 'Regalo', 'OBJ012', 'N86000012'),
('ANN013', 'Vendo auricolari Bluetooth', 'Auricolari nuovi, mai usati', CURRENT_DATE, 'Informatica', 'Attivo', 35.00, 'Vendita', 'OBJ013', 'N86000013'),
('ANN014', 'Scambio libro Matematica', 'Cerco libro di Fisica', CURRENT_DATE, 'LibriTesto', 'Attivo', NULL, 'Scambio', 'OBJ014', 'N86000014'),
('ANN015', 'Vendo borsa tracolla', 'Borsa in tessuto, ottime condizioni', CURRENT_DATE, 'Altro', 'Attivo', 28.00, 'Vendita', 'OBJ015', 'N86000015'),
('ANN016', 'Vendo smartphone Samsung', 'Smartphone in ottime condizioni', CURRENT_DATE, 'Informatica', 'Attivo', 150.00, 'Vendita', 'OBJ016', 'N86000016'),
('ANN017', 'Regalo libro Chimica', 'Libro universitario completo', CURRENT_DATE, 'LibriTesto', 'Attivo', NULL, 'Regalo', 'OBJ017', 'N86000017'),
('ANN018', 'Vendo tastiera meccanica', 'Tastiera gaming, switch blu', CURRENT_DATE, 'Informatica', 'Attivo', 80.00, 'Vendita', 'OBJ018', 'N86000018'),
('ANN019', 'Scambio bicicletta', 'Bici pieghevole, cerco skateboard', CURRENT_DATE, 'Altro', 'Attivo', NULL, 'Scambio', 'OBJ019', 'N86000019'),
('ANN020', 'Vendo chitarra acustica', 'Chitarra in perfette condizioni', CURRENT_DATE, 'Altro', 'Attivo', 120.00, 'Vendita', 'OBJ020', 'N86000020');

-- OFFERTA 
INSERT INTO Offerta (ID_Offerta, PrezzoOfferta, Commento, DataOfferta, Stato, Tipo, FK_Utente, FK_Annuncio, ID_OggettoOfferto) VALUES
('OFF001', 20.00, 'Prezzo perfetto!', CURRENT_DATE, 'Accettata', 'Vendita', 'N86000003', 'ANN001', NULL),
('OFF002', 19.00, 'Posso offrire 19 euro', CURRENT_DATE, 'Rifiutata', 'Vendita', 'N86000004', 'ANN001', NULL),
('OFF003', 15.00, 'Ottimo affare', CURRENT_DATE, 'Accettata', 'Vendita', 'N86000007', 'ANN005', NULL),
('OFF004', 30.00, 'Prezzo giusto per le scarpe', CURRENT_DATE, 'Attesa', 'Vendita', 'N86000008', 'ANN007', NULL),
('OFF005', 22.00, 'Felpa interessante', CURRENT_DATE, 'Rifiutata', 'Vendita', 'N86000015', 'ANN011', NULL),
('OFF006', 35.00, 'Auricolari perfetti', CURRENT_DATE, 'Accettata', 'Vendita', 'N86000015', 'ANN013', NULL),
('OFF007', 28.00, 'Bella borsa', CURRENT_DATE, 'Attesa', 'Vendita', 'N86000002', 'ANN015', NULL),
('OFF008', 140.00, 'Offro 140 euro per smartphone', CURRENT_DATE, 'Attesa', 'Vendita', 'N86000005', 'ANN016', NULL),
('OFF009', 75.00, 'Tastiera gaming interessante', CURRENT_DATE, 'Rifiutata', 'Vendita', 'N86000012', 'ANN018', NULL),
('OFF010', 120.00, 'Prezzo giusto per chitarra', CURRENT_DATE, 'Accettata', 'Vendita', 'N86000010', 'ANN020', NULL);

-- MODCONSEGNA per annunci con offerte accettate
INSERT INTO ModConsegna (ID_Consegna, sedeUni, oraInizioFasciaOraria, oraFineFasciaOraria, Note, Data, FK_Annuncio) VALUES
('MOD001', 'Via Claudio 21', '09:00', '12:00', 'Ritiro presso la biblioteca di Ingegneria', CURRENT_DATE + INTERVAL '3 days', 'ANN001'),
('MOD002', 'Complesso Monte S. Angelo', '14:00', '17:00', 'Consegna libro di Fisica', CURRENT_DATE + INTERVAL '5 days', 'ANN005'),
('MOD003', 'Via Claudio 21', '10:00', '13:00', 'Ritiro auricolari presso aula studio', CURRENT_DATE + INTERVAL '2 days', 'ANN013'),
('MOD004', 'Complesso Monte S. Angelo', '15:00', '18:00', 'Consegna chitarra presso cortile principale', CURRENT_DATE + INTERVAL '4 days', 'ANN020');


