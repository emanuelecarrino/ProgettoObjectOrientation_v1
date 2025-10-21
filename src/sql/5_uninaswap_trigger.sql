---- TRIGGER FUNCTIONS ------


---- PRIMO ----

CREATE OR REPLACE TRIGGER ControlloOfferta
BEFORE INSERT OR UPDATE ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_controlloOfferta();

---- SECONDO ----

CREATE OR REPLACE TRIGGER oggettoUnivocoPerAnnuncio
BEFORE INSERT OR UPDATE ON Annuncio
FOR EACH ROW
EXECUTE FUNCTION fun_oggettoUnivocoPerAnnuncio();

---- TERZO ----

CREATE OR REPLACE TRIGGER oggettoUnivocoPerScambio
BEFORE INSERT OR UPDATE ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_oggettoUnivocoPerScambio();

---- QUARTO ----

CREATE OR REPLACE TRIGGER regolaDataConsegna
BEFORE INSERT ON ModConsegna
FOR EACH ROW
EXECUTE FUNCTION fun_regolaDataConsegna();

---- QUINTO ----

CREATE OR REPLACE TRIGGER messaggioSuOfferta
BEFORE INSERT OR UPDATE ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_messaggioSuOfferta();

---- SESTO ----

CREATE OR REPLACE TRIGGER incrementoProprietari
AFTER INSERT OR UPDATE ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_incrementoProprietari();

---- SETTIMO ----

CREATE OR REPLACE TRIGGER controlloDataAnnuncio
BEFORE INSERT ON Annuncio
FOR EACH ROW
EXECUTE FUNCTION fun_controlloDataAnnuncio();

---- OTTAVO ----

CREATE OR REPLACE TRIGGER controlloOggettoOfferta
BEFORE INSERT ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_controlloOggettoOfferta();

---- NONO ----

CREATE OR REPLACE TRIGGER controlloVendite
AFTER INSERT OR UPDATE ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_controlloVendite();

---- DECIMO ----

CREATE OR REPLACE TRIGGER controlloOffertaAccettata
BEFORE INSERT OR UPDATE ON Annuncio
FOR EACH ROW
EXECUTE FUNCTION fun_controlloOffertaAccettata();

---- UNDICESIMO ----

CREATE OR REPLACE TRIGGER controlloModConsegna
BEFORE INSERT ON ModConsegna
FOR EACH ROW
EXECUTE FUNCTION fun_controlloModConsegna();

---- DODICESIMO ----

CREATE OR REPLACE TRIGGER offertaStessoVenditore
BEFORE INSERT ON Offerta
FOR EACH ROW
EXECUTE FUNCTION fun_offertaStessoVenditore();

---- TREDICESIMO ----

CREATE OR REPLACE TRIGGER offertaRipetuta
BEFORE INSERT ON Offerta 
FOR EACH ROW
EXECUTE FUNCTION fun_offertaRipetuta();


---- QUATTORDICESIMO ----

CREATE TRIGGER proprietaOggettoAnnuncio_insert
BEFORE INSERT OR UPDATE ON Annuncio
FOR EACH ROW 
EXECUTE FUNCTION fun_proprietaOggettoAnnuncio();


---- QUINDICESIMO ----

CREATE TRIGGER controlloAnnuncioCompletato
BEFORE INSERT OR UPDATE ON Annuncio
FOR EACH ROW
EXECUTE FUNCTION fun_controlloAnnuncioChiuso();

