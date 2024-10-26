package torquehub.torquehub.business.exeption.comment_exeptions;

public class CommentUpvoteException extends RuntimeException {
    public CommentUpvoteException(String message) {
        super(message);
    }

    public CommentUpvoteException(String message, Throwable cause) {
        super(message, cause);
    }
}

