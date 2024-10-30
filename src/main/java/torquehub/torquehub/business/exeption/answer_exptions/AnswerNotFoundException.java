package torquehub.torquehub.business.exeption.answer_exptions;

public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(String message) {
        super(message);
    }
}
