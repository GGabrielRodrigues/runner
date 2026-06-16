package br.ufg.inf.hubsaude.exception;

import br.ufg.inf.hubsaude.model.fhir.OperationOutcome;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public OperationOutcome handleValidationException(ValidationException ex) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue("error", ex.getErrorCode(), ex.getMessage());
        return outcome;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public OperationOutcome handleAllExceptions(Exception ex) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue("fatal", "INTERNAL_ERROR", ex.getMessage());
        return outcome;
    }
}
