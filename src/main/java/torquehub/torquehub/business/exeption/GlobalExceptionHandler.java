package torquehub.torquehub.business.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import torquehub.torquehub.business.exeption.comment_exeptions.CommentNotFoundException;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.domain.response.MessageResponse;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        MessageResponse response = new MessageResponse();
        response.setMessage(ex.getMessage());
        if (ex.getMessage().equals("Invalid credentials")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);  // 401 Unauthorized for wrong password or email
        } else if (ex.getMessage().equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);      // 404 Not Found for missing users
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);       // 409 Conflict for other cases
        }
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        MessageResponse response = new MessageResponse();
        response.setMessage("An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);  // 500 Internal Server Error
    }

    @ExceptionHandler(InvalidAccessTokenException.class)
    public ResponseEntity<String> handleInvalidAccessToken(InvalidAccessTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<MessageResponse> handleCommentNotFoundException(CommentNotFoundException ex) {
        MessageResponse response = new MessageResponse();
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }

}
