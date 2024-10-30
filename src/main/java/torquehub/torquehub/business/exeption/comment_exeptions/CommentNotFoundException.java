package torquehub.torquehub.business.exeption.comment_exeptions;

public class CommentNotFoundException extends RuntimeException {
  public CommentNotFoundException(String message) {
    super(message);
  }

}
