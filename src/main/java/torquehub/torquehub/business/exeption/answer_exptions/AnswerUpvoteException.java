package torquehub.torquehub.business.exeption.answer_exptions;

public class AnswerUpvoteException extends RuntimeException {
    public AnswerUpvoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnswerUpvoteException(String message) {
        super(message);
    }
}
