package torquehub.torquehub.business.exeption.tag_exeptions;

public class TagDeleteExeption extends RuntimeException {

    public TagDeleteExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
