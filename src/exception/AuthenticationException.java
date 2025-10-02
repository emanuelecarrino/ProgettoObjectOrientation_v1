package exception;

/** Errore di autenticazione (login fallito, credenziali errate). */
public class AuthenticationException extends ApplicationException {
    public AuthenticationException() { super("Errore di autenticazione"); }
    public AuthenticationException(String message) { super(message); }
}
