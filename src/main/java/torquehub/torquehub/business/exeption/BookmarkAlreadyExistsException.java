package torquehub.torquehub.business.exeption;

public class BookmarkAlreadyExistsException extends RuntimeException {
    public BookmarkAlreadyExistsException(String message) {
        super(message);
    }
}
