package exception;

/** Violazione di unicità (email, username, ecc.). */
public class DuplicateResourceException extends ApplicationException {
    public DuplicateResourceException() { super("Risorsa duplicata"); }
    public DuplicateResourceException(String message) { super(message); }
}
