package torquehub.torquehub.business.exeption.role_exeptions;

public class RoleDeleteExeption extends RuntimeException {

    public RoleDeleteExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
