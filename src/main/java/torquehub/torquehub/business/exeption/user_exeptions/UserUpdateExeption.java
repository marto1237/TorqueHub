package torquehub.torquehub.business.exeption.user_exeptions;

public class UserUpdateExeption extends RuntimeException {
    public UserUpdateExeption(String message) {
        super(message);
    }

    public UserUpdateExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
