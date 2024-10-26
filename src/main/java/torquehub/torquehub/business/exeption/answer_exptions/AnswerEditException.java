package torquehub.torquehub.business.exeption.answer_exptions;

public class AnswerEditException extends RuntimeException {
    public AnswerEditException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnswerEditException(String message) {
        super(message);
    }
}