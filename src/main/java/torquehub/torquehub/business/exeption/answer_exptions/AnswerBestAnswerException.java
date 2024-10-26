package torquehub.torquehub.business.exeption.answer_exptions;

public class AnswerBestAnswerException extends RuntimeException {
    public AnswerBestAnswerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnswerBestAnswerException(String message) {
        super(message);
    }
}
