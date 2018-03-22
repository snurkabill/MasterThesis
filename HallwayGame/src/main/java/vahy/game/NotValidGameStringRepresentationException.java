package vahy.game;

public class NotValidGameStringRepresentationException extends Exception {

    public NotValidGameStringRepresentationException(String message) {
        super(message);
    }

    public NotValidGameStringRepresentationException(String message, Throwable cause) {
        super(message, cause);
    }
}
