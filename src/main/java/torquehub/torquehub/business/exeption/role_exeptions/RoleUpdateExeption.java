package torquehub.torquehub.business.exeption.role_exeptions;

public class RoleUpdateExeption extends RuntimeException {
    public RoleUpdateExeption(String message) {
        super(message);
    }

    public RoleUpdateExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
