package torquehub.torquehub.business.exeption.answer_exptions;

public class BestAnswerApprovalException extends RuntimeException {
    public BestAnswerApprovalException(String message, Throwable cause) {
        super(message, cause);
    }

    public BestAnswerApprovalException(String message) {
        super(message);
    }
}