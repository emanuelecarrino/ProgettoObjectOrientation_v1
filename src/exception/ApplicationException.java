package exception;

/**
 * Base generica per eccezioni applicative controllate.
 */
public class ApplicationException extends Exception {
    public ApplicationException() { super("Errore applicativo"); }
    public ApplicationException(String message) { super(message); }
    public ApplicationException(String message, Throwable cause) { super(message, cause); }
}
