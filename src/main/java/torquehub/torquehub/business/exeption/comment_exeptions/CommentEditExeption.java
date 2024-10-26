package torquehub.torquehub.business.exeption.comment_exeptions;

public class CommentEditExeption extends RuntimeException {
    public CommentEditExeption(String message) {
        super(message);
    }

  public CommentEditExeption(String message, Throwable cause) {
    super(message, cause);
  }
}
