package torquehub.torquehub.business.exeption.user_exeptions;

public class UserCreateUserExeption extends RuntimeException {
    public UserCreateUserExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
