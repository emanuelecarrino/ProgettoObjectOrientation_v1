package exception;

/** Risorsa non trovata (es. utente / annuncio inesistente). */
public class NotFoundException extends ApplicationException {
    public NotFoundException() { super("Risorsa non trovata"); }
    public NotFoundException(String message) { super(message); }
}
