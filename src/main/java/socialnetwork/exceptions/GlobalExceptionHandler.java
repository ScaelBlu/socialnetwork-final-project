package socialnetwork.exceptions;

import org.apache.tomcat.util.http.fileupload.impl.SizeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail notFoundHandler(EntityNotFoundException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setType(URI.create("socialnetwork/not-found"));
        return detail;
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ProblemDetail constraintViolationHandler(SQLIntegrityConstraintViolationException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        detail.setType(URI.create("socialnetwork/invalid-arguments"));
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidArgumentsHandler(MethodArgumentNotValidException e) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_ACCEPTABLE);
        detail.setDetail(e.getBindingResult().getFieldError().getDefaultMessage());
        detail.setType(URI.create("socialnetwork/invalid-arguments"));
        return detail;
    }

    @ExceptionHandler(NoSuchRelationshipException.class)
    public ProblemDetail noSuchRelationshipHandler(NoSuchRelationshipException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setType(URI.create("socialnetwork/not-found"));
        return detail;
    }

    @ExceptionHandler(SizeException.class)
    public ProblemDetail fileSizeLimitHandler(SizeException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
        detail.setType(URI.create("socialnetwork/image-too-large"));
        return detail;
    }

    @ExceptionHandler(SameUserRelationshipException.class)
    public ProblemDetail sameUserHandler(SameUserRelationshipException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        detail.setType(URI.create("socialnetwork/same-user-relationship"));
        return detail;
    }
}
