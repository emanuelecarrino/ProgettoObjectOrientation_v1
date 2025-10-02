package exception;

/** Errore generico */
public class PersistenceException extends ApplicationException {
    public PersistenceException() { super("Errore di persistenza"); }
    public PersistenceException(String message, Throwable cause) { super(message, cause); }
}
