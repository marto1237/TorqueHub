package torquehub.torquehub.business.exeption.comment_exeptions;

public class CommentDeleteExeption extends RuntimeException {

    public CommentDeleteExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
