package torquehub.torquehub.business.exeption.comment_exeptions;

public class CommentDownvoteException extends RuntimeException {

    public CommentDownvoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
