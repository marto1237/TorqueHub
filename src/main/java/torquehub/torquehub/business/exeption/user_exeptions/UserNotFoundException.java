package torquehub.torquehub.business.exeption.user_exeptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
