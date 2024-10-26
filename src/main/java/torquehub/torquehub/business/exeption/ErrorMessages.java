package torquehub.torquehub.business.exeption;

public class ErrorMessages {

    private ErrorMessages() {
        // Throw an exception if this ever *is* called
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String USER_NOT_FOUND = "User not found";
    public static final String QUESTION_NOT_FOUND = "Question not found";
    public static final String ANSWER_NOT_FOUND = "Answer not found";
}
