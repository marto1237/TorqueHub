package torquehub.torquehub.business.exeption.question_exeptions;

public class QuestionCreationException extends RuntimeException {
    public QuestionCreationException(String message) {
        super(message);
    }

    public QuestionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
