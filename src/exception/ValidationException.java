package exception;

/** Eccezione per errori di validazione input domain (campi mancanti / formati errati). */
public class ValidationException extends ApplicationException {
    public ValidationException() { super("Errore di validazione"); }
    public ValidationException(String message) { super(message); }
}
