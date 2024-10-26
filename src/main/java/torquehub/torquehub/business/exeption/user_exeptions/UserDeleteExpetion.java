package torquehub.torquehub.business.exeption.user_exeptions;

public class UserDeleteExpetion extends RuntimeException {
    public UserDeleteExpetion(String message) {
        super(message);
    }

    public UserDeleteExpetion(String message, Throwable cause) {
        super(message, cause);
    }
}
